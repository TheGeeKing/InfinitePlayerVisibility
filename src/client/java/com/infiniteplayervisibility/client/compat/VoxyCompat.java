package com.infiniteplayervisibility.client.compat;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.OptionalInt;

public final class VoxyCompat {
	private static final String VOXY_MOD_ID = "voxy";
	private static final int VISIBILITY_CACHE_MAX_ENTRIES = 256;
	private static final String VOXY_RENDER_SYSTEM_CLASS = "me.cortex.voxy.client.core.VoxyRenderSystem";
	private static final String VOXY_WORLD_ENGINE_CLASS = "me.cortex.voxy.common.world.WorldEngine";
	private static final String VOXY_WORLD_SECTION_CLASS = "me.cortex.voxy.common.world.WorldSection";
	private static final String VOXY_MAPPER_CLASS = "me.cortex.voxy.common.world.other.Mapper";
	private static final String GET_ENGINE_METHOD = "getEngine";
	private static final String GET_MAPPER_METHOD = "getMapper";
	private static final String ACQUIRE_SECTION_METHOD = "acquireIfExists";
	private static final String GET_BLOCK_OPACITY_METHOD = "getBlockStateOpacity";
	private static final String GET_RAW_DATA_METHOD = "_unsafeGetRawDataArray";
	private static final String RELEASE_SECTION_METHOD = "release";
	private static final String GET_SECTION_INDEX_METHOD = "getIndex";
	private static final String GET_RENDER_SYSTEM_METHOD = "voxy$getRenderSystem";
	private static final String MAX_LOD_LAYER_FIELD = "MAX_LOD_LAYER";

	private static final boolean VOXY_LOADED = FabricLoader.getInstance().isModLoaded(VOXY_MOD_ID);
	private static final Reflection REFLECTION = VOXY_LOADED ? Reflection.create() : null;
	private static final Map<Integer, CacheEntry> VISIBILITY_CACHE = new HashMap<>();
	private static final int MAX_SAMPLES = 256;
	private static final double MIN_STEP = 2.0D;
	private static final double MAX_STEP = 16.0D;
	private static final double START_OFFSET = 1.5D;
	private static final double END_PADDING = 0.75D;

	private VoxyCompat() {
	}

	public static boolean shouldRenderPlayer(net.minecraft.world.entity.player.Player player) {
		return shouldRenderEntity(player);
	}

	public static boolean shouldRenderUnloadedPlayer(net.minecraft.world.entity.player.Player player) {
		return shouldRenderEntity(player);
	}

	public static OptionalInt getConfiguredRenderDistanceBlocks() {
		return OptionalInt.empty();
	}

	public static boolean shouldRenderEntity(Entity entity) {
		if (!usesVoxyOcclusion(entity)) {
			return true;
		}

		if (REFLECTION == null || REFLECTION.isBroken()) {
			return true;
		}

		Minecraft client = Minecraft.getInstance();
		if (client.level == null || client.gameRenderer == null || client.levelRenderer == null) {
			return true;
		}

		Entity cameraEntity = client.getCameraEntity();
		if (cameraEntity == null || cameraEntity == entity) {
			return true;
		}

		Vec3 cameraPos = client.gameRenderer.getMainCamera().position();
		BlockPos cameraBlockPos = BlockPos.containing(cameraPos);
		BlockPos entityBlockPos = entity.blockPosition();
		long worldTime = client.level.getGameTime();

		Boolean cachedVisibility = getCachedVisibility(entity, worldTime, cameraBlockPos, entityBlockPos);
		if (cachedVisibility != null) {
			return cachedVisibility;
		}

		boolean visible = isVisibleWithVoxy(client.levelRenderer, cameraPos, entity);
		cacheVisibility(entity, worldTime, cameraBlockPos, entityBlockPos, visible);
		return visible;
	}

	private static Boolean getCachedVisibility(Entity entity, long worldTime, BlockPos cameraBlockPos, BlockPos entityBlockPos) {
		CacheEntry cached = VISIBILITY_CACHE.get(entity.getId());
		return cached != null && cached.matches(worldTime, cameraBlockPos, entityBlockPos) ? cached.visible() : null;
	}

	private static void cacheVisibility(Entity entity, long worldTime, BlockPos cameraBlockPos, BlockPos entityBlockPos, boolean visible) {
		VISIBILITY_CACHE.put(entity.getId(), new CacheEntry(worldTime, cameraBlockPos, entityBlockPos, visible));
		pruneCache(worldTime);
	}

	private static boolean usesVoxyOcclusion(Entity entity) {
		return entity instanceof Player || entity instanceof LivingEntity;
	}

	private static boolean isVisibleWithVoxy(LevelRenderer worldRenderer, Vec3 cameraPos, Entity entity) {
		Object renderSystem = REFLECTION.getRenderSystem(worldRenderer);
		if (renderSystem == null) {
			return true;
		}

		Object engine = REFLECTION.getEngine(renderSystem);
		Object mapper = REFLECTION.getMapper(engine);
		if (engine == null || mapper == null) {
			return true;
		}

		for (Vec3 samplePoint : getSamplePoints(entity)) {
			if (!isOccluded(engine, mapper, cameraPos, samplePoint)) {
				return true;
			}
		}

		return false;
	}

	private static boolean isOccluded(Object engine, Object mapper, Vec3 start, Vec3 end) {
		Vec3 delta = end.subtract(start);
		double distance = delta.length();
		if (distance <= START_OFFSET + END_PADDING) {
			return false;
		}

		Vec3 direction = delta.scale(1.0D / distance);
		double maxDistance = distance - END_PADDING;
		double step = Mth.clamp(distance / MAX_SAMPLES, MIN_STEP, MAX_STEP);

		for (double travelled = START_OFFSET; travelled < maxDistance; travelled += step) {
			Vec3 sample = start.add(direction.scale(travelled));
			if (isOpaqueAt(engine, mapper, sample)) {
				return true;
			}
		}

		return false;
	}

	private static boolean isOpaqueAt(Object engine, Object mapper, Vec3 point) {
		int blockX = Mth.floor(point.x);
		int blockY = Mth.floor(point.y);
		int blockZ = Mth.floor(point.z);

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
		if (VISIBILITY_CACHE.size() <= VISIBILITY_CACHE_MAX_ENTRIES) {
			return;
		}

		Iterator<Map.Entry<Integer, CacheEntry>> iterator = VISIBILITY_CACHE.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Integer, CacheEntry> entry = iterator.next();
			if (worldTime - entry.getValue().worldTime() > 1L) {
				iterator.remove();
			}
		}
	}

	private static Vec3[] getSamplePoints(Entity entity) {
		AABB box = entity.getBoundingBox();
		Vec3 center = box.getCenter();
		if (entity instanceof LivingEntity livingEntity) {
			double eyeY = Mth.clamp(livingEntity.getEyeY(), box.minY + 0.2D, box.maxY - 0.05D);
			double chestY = Mth.lerp(0.55D, box.minY, box.maxY);
			double shoulderX = Math.min(0.25D, box.getXsize() * 0.25D);
			double shoulderZ = Math.min(0.25D, box.getZsize() * 0.25D);

			return new Vec3[] {
				new Vec3(center.x, eyeY, center.z),
				new Vec3(center.x, chestY, center.z),
				new Vec3(center.x + shoulderX, chestY, center.z),
				new Vec3(center.x - shoulderX, chestY, center.z),
				new Vec3(center.x, chestY, center.z + shoulderZ),
				new Vec3(center.x, chestY, center.z - shoulderZ)
			};
		}

		return new Vec3[] {center};
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
				Class<?> renderSystemClass = Class.forName(VOXY_RENDER_SYSTEM_CLASS);
				Class<?> worldEngineClass = Class.forName(VOXY_WORLD_ENGINE_CLASS);
				Class<?> worldSectionClass = Class.forName(VOXY_WORLD_SECTION_CLASS);
				Class<?> mapperClass = Class.forName(VOXY_MAPPER_CLASS);

				return new Reflection(
						renderSystemClass.getMethod(GET_ENGINE_METHOD),
						worldEngineClass.getMethod(GET_MAPPER_METHOD),
						worldEngineClass.getMethod(ACQUIRE_SECTION_METHOD, int.class, int.class, int.class, int.class),
						mapperClass.getMethod(GET_BLOCK_OPACITY_METHOD, long.class),
						worldSectionClass.getMethod(GET_RAW_DATA_METHOD),
						worldSectionClass.getMethod(RELEASE_SECTION_METHOD),
						worldSectionClass.getMethod(GET_SECTION_INDEX_METHOD, int.class, int.class, int.class),
						worldEngineClass.getField(MAX_LOD_LAYER_FIELD).getInt(null)
				);
			} catch (ReflectiveOperationException exception) {
				return null;
			}
		}

		private boolean isBroken() {
			return this.broken;
		}

		private Object getRenderSystem(LevelRenderer worldRenderer) {
			try {
				if (this.getRenderSystemMethod == null) {
					this.getRenderSystemMethod = worldRenderer.getClass().getMethod(GET_RENDER_SYSTEM_METHOD);
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
