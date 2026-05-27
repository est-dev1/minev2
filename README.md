# ClientTracker

A Paper/Spigot plugin that detects which Minecraft client players are using and tracks resource pack status, notifying admins directly in chat.

## Features

- Detects client brand on join (Fabric, Lunar Client, Vanilla Minecraft Launcher, Forge, and more)
- Tracks resource pack accept / decline / fail status per player
- Sends colour-coded alerts to all online admins in Minecraft chat
- Logs everything to the server console

## Supported Clients

| Client | Detected As |
|---|---|
| Minecraft Launcher | Minecraft Launcher (Vanilla) |
| Fabric | Fabric Launcher |
| Lunar Client | Lunar Client |
| Forge | Forge / Forge (FML) |
| Other | Raw brand name |

## Build

Requires **Java JDK 17+** and **Maven**.

```bash
git clone https://github.com/est-dev1/minev2.git
cd minev2
mvn package
```

JAR will be at `target/ClientTracker-1.0.0.jar`.

## Install

1. Drop `ClientTracker-1.0.0.jar` into your server's `plugins/` folder.
2. Restart the server.

## Permissions

| Permission | Description | Default |
|---|---|---|
| `clienttracker.notify` | Receive client & resource pack alerts in chat | OP |

### Grant to a non-OP (LuckPerms)
```
/lp user <name> permission set clienttracker.notify true
```

## Adding More Clients

Edit `BRAND_MAP` in `ClientTrackerPlugin.java`:

```java
BRAND_MAP.put("badlion", "Badlion Client");
BRAND_MAP.put("optifine", "OptiFine");
```

Then rebuild with `mvn package`.
