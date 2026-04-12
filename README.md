# Infinite Player Visibility

Mod Fabric pour `Minecraft 1.21.11` qui garde les joueurs visibles a tres longue distance sans charger leurs chunks.

## Comportement

- Le serveur continue a synchroniser les entites joueurs meme hors distance de suivi normale.
- Le client accepte de rendre ces joueurs meme si leur chunk n'est pas charge.
- Les chunks lointains ne sont pas charges pour autant.

## Installation

Installe le meme jar sur le serveur et sur les clients. Le cote serveur gere le tracking reseau, et le cote client leve les limites de rendu.

## Build

```powershell
.\gradlew.bat build
```

Le jar remappe se trouvera dans `build/libs/`.
