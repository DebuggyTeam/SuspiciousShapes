# Suspicious Shapes

glTF models for Minecraft blocks

When installed, this mod passively resolves references to .gltf models from blockstate json.

Textures are a bit different, and may require hand-editing of the gltf's texture data URIs! More on that later when the mod is done!

## Using Suspicious Shapes

(todo)
(there is no repository yet, and this is a Jar-in-Jar mod anyway)
(so you'll have to use publishToMavenLocal to create a local repo copy, then use it for your project)
(or just drop it in the mods folder, I'm not your mom)

## Issues

Please report any issues you find at https://github.com/DebuggyTeam/SuspiciousShapes/issues
Or visit the Debuggy discord at https://discord.gg/bAXYSs8gKQ

## Building From Source

In Linux or OSX, clone the repository, and from the terminal in the repository directory, enter:

```
./gradlew clean build
```

In Windows, clone the repository, and from the command prompt or powershell, enter:
```
gradlew clean build
```

The jar will appear in the build/libs folder


## Contributing

If you have an idea for how to make Suspicious Shapes better, head on over to the Debuggy discord or make a pull request.

Style for this project is Google Style ( https://google.github.io/styleguide/javaguide.html ) with one exception: whitespace. Tabs are used for indentation for accessibility reasons, while spaces are retained for non-hierarchical alignment which tabs cannot do. If changing the tab width misaligns things, you're doing it wrong. We assume a monospaced font, but there aren't any font police, so your comic sans secret is safe with me.

Clarity is the most important thing. Other people will have to touch the code, including you from six months in the future. They (or you) should be able to get the gist of what the code does without any comments. If you're unsure of how to proceed, talk to us! We've been doing this a long time.
