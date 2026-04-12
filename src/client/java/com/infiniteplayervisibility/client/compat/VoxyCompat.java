package com.infiniteplayervisibility.client.compat;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class VoxyCompat {
	private static final boolean VOXY_LOADED = FabricLoader.getInstance().isModLoaded("voxy");
	private static final Reflection REFLECTION = VOXY_LOADED ? Reflection.create() : null;
	private static final Map<Integer, CacheEntry> CACHE = new HashMap<>();
	private static final int MAX_SAMPLES = 256;
	private static final double MIN_STEP = 2.0D;
	private static final double MAX_STEP = 16.0D;
	private static final double START_OFFSET = 1.5D;
	private static final double END_PADDING = 0.75D;

	private VoxyCompat() {
	}

	public static boolean shouldRenderPlayer(net.minecraft.entity.player.PlayerEntity player) {
		return shouldRenderEntity(player);
	}

	public static boolean shouldRenderUnloadedPlayer(net.minecraft.entity.player.PlayerEntity player) {
		return shouldRenderEntity(player);
	}

	public static boolean shouldRenderEntity(Entity entity) {
		if (!usesVoxyOcclusion(entity)) {
			return true;
		}

		if (REFLECTION == null || REFLECTION.isBroken()) {
			return true;
		}

		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world == null || client.gameRenderer == null || client.worldRenderer == null) {
			return true;
		}

		Entity cameraEntity = client.getCameraEntity();
		if (cameraEntity == null || cameraEntity == entity) {
			return true;
		}

		Vec3d cameraPos = client.gameRenderer.getCamera().getCameraPos();
		BlockPos cameraBlockPos = BlockPos.ofFloored(cameraPos);
		BlockPos entityBlockPos = entity.getBlockPos();
		long worldTime = client.world.getTime();

		CacheEntry cached = CACHE.get(entity.getId());
		if (cached != null && cached.matches(worldTime, cameraBlockPos, entityBlockPos)) {
			return cached.visible();
		}

		boolean visible = isVisibleWithVoxy(client.worldRenderer, cameraPos, entity);
		CACHE.put(entity.getId(), new CacheEntry(worldTime, cameraBlockPos, entityBlockPos, visible));
		pruneCache(worldTime);
		return visible;
	}

	private static boolean usesVoxyOcclusion(Entity entity) {
		return entity instanceof PlayerEntity || entity instanceof LivingEntity;
	}

	private static boolean isVisibleWithVoxy(WorldRenderer worldRenderer, Vec3d cameraPos, Entity entity) {
		Object renderSystem = REFLECTION.getRenderSystem(worldRenderer);
		if (renderSystem == null) {
			return true;
		}

		Object engine = REFLECTION.getEngine(renderSystem);
		Object mapper = REFLECTION.getMapper(engine);
		if (engine == null || mapper == null) {
			return true;
		}

		for (Vec3d samplePoint : getSamplePoints(entity)) {
			if (!isOccluded(engine, mapper, cameraPos, samplePoint)) {
				return true;
			}
		}

		return false;
	}

	private static boolean isOccluded(Object engine, Object mapper, Vec3d start, Vec3d end) {
		Vec3d delta = end.subtract(start);
		double distance = delta.length();
		if (distance <= START_OFFSET + END_PADDING) {
			return false;
		}

		Vec3d direction = delta.multiply(1.0D / distance);
		double maxDistance = distance - END_PADDING;
		double step = MathHelper.clamp(distance / MAX_SAMPLES, MIN_STEP, MAX_STEP);

		for (double travelled = START_OFFSET; travelled < maxDistance; travelled += step) {
			Vec3d sample = start.add(direction.multiply(travelled));
			if (isOpaqueAt(engine, mapper, sample)) {
				return true;
			}
		}

		return false;
	}

	private static boolean isOpaqueAt(Object engine, Object mapper, Vec3d point) {
		int blockX = MathHelper.floor(point.x);
		int blockY = MathHelper.floor(point.y);
		int blockZ = MathHelper.floor(point.z);

		for (int level = 0; level <= REFLECTION.maxLodLevel; level++) {
			Long mappingId = REFLECTION.getMappingId(engine, level, blockX, blockY, blockZ);
			if (mappingId == null) {
				continue;
			}

			return mappingId != 0L && REFLECTION.getOpacity(mapper, mappingId) > 0;
		}

		return false;
	}

	private static void pruneCache(long worldTime) {
		if (CACHE.size() <= 256) {
			return;
		}

		Iterator<Map.Entry<Integer, CacheEntry>> iterator = CACHE.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Integer, CacheEntry> entry = iterator.next();
			if (worldTime - entry.getValue().worldTime() > 1L) {
				iterator.remove();
			}
		}
	}

	private static Vec3d[] getSamplePoints(Entity entity) {
		Box box = entity.getBoundingBox();
		Vec3d center = box.getCenter();
		if (entity instanceof LivingEntity livingEntity) {
			double eyeY = MathHelper.clamp(livingEntity.getEyeY(), box.minY + 0.2D, box.maxY - 0.05D);
			double chestY = MathHelper.lerp(0.55D, box.minY, box.maxY);
			double shoulderX = Math.min(0.25D, box.getLengthX() * 0.25D);
			double shoulderZ = Math.min(0.25D, box.getLengthZ() * 0.25D);

			return new Vec3d[] {
				new Vec3d(center.x, eyeY, center.z),
				new Vec3d(center.x, chestY, center.z),
				new Vec3d(center.x + shoulderX, chestY, center.z),
				new Vec3d(center.x - shoulderX, chestY, center.z),
				new Vec3d(center.x, chestY, center.z + shoulderZ),
				new Vec3d(center.x, chestY, center.z - shoulderZ)
			};
		}

		return new Vec3d[] {center};
	}

	private record CacheEntry(long worldTime, BlockPos cameraPos, BlockPos playerPos, boolean visible) {
		private boolean matches(long worldTime, BlockPos cameraPos, BlockPos playerPos) {
			return this.worldTime == worldTime && this.cameraPos.equals(cameraPos) && this.playerPos.equals(playerPos);
		}
	}

	private static final class Reflection {
		private final Method getEngineMethod;
		private final Method getMapperMethod;
		private final Method acquireIfExistsMethod;
		private final Method getBlockStateOpacityMethod;
		private final Method getRawDataMethod;
		private final Method releaseMethod;
		private final Method getIndexMethod;
		private final int maxLodLevel;
		private Method getRenderSystemMethod;
		private boolean broken;

		private Reflection(Method getEngineMethod, Method getMapperMethod, Method acquireIfExistsMethod, Method getBlockStateOpacityMethod, Method getRawDataMethod, Method releaseMethod, Method getIndexMethod, int maxLodLevel) {
			this.getEngineMethod = getEngineMethod;
			this.getMapperMethod = getMapperMethod;
			this.acquireIfExistsMethod = acquireIfExistsMethod;
			this.getBlockStateOpacityMethod = getBlockStateOpacityMethod;
			this.getRawDataMethod = getRawDataMethod;
			this.releaseMethod = releaseMethod;
			this.getIndexMethod = getIndexMethod;
			this.maxLodLevel = maxLodLevel;
		}

		private static Reflection create() {
			try {
				Class<?> renderSystemClass = Class.forName("me.cortex.voxy.client.core.VoxyRenderSystem");
				Class<?> worldEngineClass = Class.forName("me.cortex.voxy.common.world.WorldEngine");
				Class<?> worldSectionClass = Class.forName("me.cortex.voxy.common.world.WorldSection");
				Class<?> mapperClass = Class.forName("me.cortex.voxy.common.world.other.Mapper");

				return new Reflection(
						renderSystemClass.getMethod("getEngine"),
						worldEngineClass.getMethod("getMapper"),
						worldEngineClass.getMethod("acquireIfExists", int.class, int.class, int.class, int.class),
						mapperClass.getMethod("getBlockStateOpacity", long.class),
						worldSectionClass.getMethod("_unsafeGetRawDataArray"),
						worldSectionClass.getMethod("release"),
						worldSectionClass.getMethod("getIndex", int.class, int.class, int.class),
						worldEngineClass.getField("MAX_LOD_LAYER").getInt(null)
				);
			} catch (ReflectiveOperationException exception) {
				return null;
			}
		}

		private boolean isBroken() {
			return this.broken;
		}

		private Object getRenderSystem(WorldRenderer worldRenderer) {
			try {
				if (this.getRenderSystemMethod == null) {
					this.getRenderSystemMethod = worldRenderer.getClass().getMethod("voxy$getRenderSystem");
				}

				return this.getRenderSystemMethod.invoke(worldRenderer);
			} catch (ReflectiveOperationException exception) {
				this.broken = true;
				return null;
			}
		}

		private Object getEngine(Object renderSystem) {
			return this.invoke(this.getEngineMethod, renderSystem);
		}

		private Object getMapper(Object engine) {
			return this.invoke(this.getMapperMethod, engine);
		}

		private Long getMappingId(Object engine, int level, int blockX, int blockY, int blockZ) {
			int sectionSpan = 32 << level;
			int sectionX = Math.floorDiv(blockX, sectionSpan);
			int sectionY = Math.floorDiv(blockY, sectionSpan);
			int sectionZ = Math.floorDiv(blockZ, sectionSpan);

			Object section = this.invoke(this.acquireIfExistsMethod, engine, level, sectionX, sectionY, sectionZ);
			if (section == null) {
				return null;
			}

			try {
				long[] data = (long[]) this.getRawDataMethod.invoke(section);
				int localX = Math.floorMod(blockX, sectionSpan) >> level;
				int localY = Math.floorMod(blockY, sectionSpan) >> level;
				int localZ = Math.floorMod(blockZ, sectionSpan) >> level;
				int index = (int) this.getIndexMethod.invoke(null, localX, localY, localZ);
				return data[index];
			} catch (ReflectiveOperationException exception) {
				this.broken = true;
				return null;
			} finally {
				try {
					this.releaseMethod.invoke(section);
				} catch (ReflectiveOperationException exception) {
					this.broken = true;
				}
			}
		}

		private int getOpacity(Object mapper, long mappingId) {
			Object opacity = this.invoke(this.getBlockStateOpacityMethod, mapper, mappingId);
			return opacity instanceof Integer integer ? integer : 0;
		}

		private Object invoke(Method method, Object instance, Object... args) {
			try {
				return method.invoke(instance, args);
			} catch (ReflectiveOperationException exception) {
				this.broken = true;
				return null;
			}
		}
	}
}
