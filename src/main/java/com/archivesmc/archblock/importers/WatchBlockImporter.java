package com.archivesmc.archblock.importers;

import com.archivesmc.archblock.Plugin;
import com.archivesmc.archblock.runnables.migration.ImportThread;
import com.archivesmc.archblock.utils.Point2D;
import com.archivesmc.archblock.utils.Point3D;
import com.archivesmc.archblock.utils.Utils;
import org.apache.commons.lang.StringUtils;
import org.yaml.snakeyaml.Yaml;
import tk.minecraftopia.watchblock.WatchBlock;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class WatchBlockImporter implements Importer{
    private Plugin plugin;
    private WatchBlock watchBlockPlugin;
    private File watchBlockConfigDir;

    private final List<String> worlds = new ArrayList<>();
    private final List<String> failedUsers = new ArrayList<>();

    public WatchBlockImporter(Plugin plugin) {
        this.plugin = plugin;
        this.watchBlockPlugin = (WatchBlock) this.plugin.getServer().getPluginManager().getPlugin("WatchBlock");
        this.watchBlockConfigDir = this.watchBlockPlugin.getDataFolder();
    }

    @Override
    public Boolean doImport() {
        this.info("Beginning WatchBlock import..");

        Boolean result;

        result = this.getWorlds();

        if (!result) {
            return result;
        }

//        result = this.convertFriends();
//
//        if (!result) {
//            return result;
//        }

        Boolean allWorldsDone = true;

        for (String world : this.worlds) {
            if (!this.convertWorld(world)) {
                allWorldsDone = false;
            }
        }

        if (!allWorldsDone) {
            this.warning("Not all worlds were converted. Please check for errors!");
        }

        this.info(
                "Disabling WatchBlock now. Please remember to remove it before you " +
                "restart next, or you'll have problems!"
        );

        this.plugin.getServer().getPluginManager().disablePlugin(this.watchBlockPlugin);

        this.info("Import complete!");

        return true;
    }

    private Boolean getWorlds() {
        this.info("Looking for worlds to import..");

        List<File> dirs = Utils.listDirectories(this.watchBlockConfigDir);

        for (File f : dirs) {
            this.worlds.add(f.getName());
        }

        this.info(String.format(
                "Found worlds (%s): %s", this.worlds.size(), StringUtils.join(this.worlds, ", ")
        ));

        return true;
    }

    private Boolean convertFriends() {
        this.info("Converting friendships..");

        File friendsFile = new File(this.watchBlockConfigDir, "allow.yml");

        Yaml yaml = new Yaml();
        FileInputStream in = null;

        try {
            in = new FileInputStream(friendsFile);
            Map<String, Map<String, Boolean>> data = (Map) ((Map) yaml.load(in)).get("allow");
            Map<String, Boolean> friends;

            String left;
            String right;

            UUID leftUuid;
            UUID rightUuid;

            String player;

            for (Map.Entry<String, Map<String, Boolean>> entry : data.entrySet()) {
                player = entry.getKey();
                friends = entry.getValue();

                this.doFetchUuid(player);

                for (String friend : friends.keySet()) {
                    this.doFetchUuid(friend);
                }

                left = this.plugin.getApi().getUuidForUsername(player);

                if (left != null) {
                    leftUuid = UUID.fromString(left);

                    for (String friend : friends.keySet()) {
                        right = this.plugin.getApi().getUuidForUsername(friend);

                        if (right != null) {
                            rightUuid = UUID.fromString(right);

                            if (! this.plugin.getApi().hasFriendship(leftUuid, rightUuid)) {
                                this.plugin.getApi().createFriendship(leftUuid, rightUuid);
                                this.info(String.format(
                                        "Created friendship: %s -> %s",
                                        player, friend
                                ));
                            }
                        }
                    }
                }
            }
        } catch (FileNotFoundException | InterruptedException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        this.info("Friendships converted.");
        return true;
    }

    private Boolean convertWorld(String world) {
        this.info(String.format(
                "Loading blocks for world: %s", world
        ));

        List<Map<Point3D, String>> points = new ArrayList<>();
        File worldDir = new File(this.watchBlockConfigDir, "/" + world);

        if (!worldDir.exists() || !worldDir.isDirectory()) {
            this.warning("Unable to find data files!");
            return false;
        }

        Point2D chunkPoint;
        Map<Point3D, String> chunkMap;

        for (File file : worldDir.listFiles()) {
            chunkPoint = this.pointFromFilename(file.getName());

            if (chunkPoint == null) {
                this.warning(String.format(
                        "Unable to get chunk for file: %s", file.getName()
                ));

                continue;
            }

            chunkMap = this.getPointsFromFile(file, chunkPoint);

            if (chunkMap == null) {
                continue;
            }

            points.add(chunkMap);
        }

        Integer doneChunks = 0;
        Integer totalChunks = points.size();

        this.info(String.format(
                "Importing %s chunks. This may take a while.", totalChunks
        ));

        List<ImportThread> threads = new ArrayList<>();
        List<ImportThread> operatingThreads = new ArrayList<>();
        
        Set<ImportThread> doneThreads = new HashSet<>();
        
        List<Map<Point3D, String>> chunks;

        int cores = Runtime.getRuntime().availableProcessors();
        int i;
        int j;
        int remaining;
        
        ImportThread th;
        
        while (points.size() > 0) {
            chunks = new ArrayList<>();

            for (j = 0; j < 25; j += 1) {
                if (points.size() < 1) {
                    break;
                }

                chunks.add(points.remove(0));
            }

            if (chunks.size() < 1) {
                break;
            }

            th = new ImportThread(world, chunks, this.plugin);
            this.info(String.format("Setting up thread: %s", th));
            threads.add(th);
        }
        
        while (threads.size() > 0 || operatingThreads.size() > 0) {
            if (operatingThreads.size() < cores) {
                remaining = threads.size();
                
                for (i = 0; i < remaining; i += 1) {
                    if (operatingThreads.size() < cores && threads.size() > 0) {
                        th = threads.remove(0);
                        operatingThreads.add(th);
                        th.start();
                    } else {
                        break;
                    }
                }
            }
            
            for (Object thr : operatingThreads.toArray()) {
                // So we can safely remove stuff from it while we iterate over it
                th = (ImportThread) thr;
                
                if (doneThreads.contains(th)) {
                    continue;
                }

                if (th.getDone()) {
                    this.info(String.format("Thread completed: %s", th));
                    doneThreads.add(th);
                    operatingThreads.remove(th);
                    doneChunks += th.getNumberOfChunks();

                    this.info(String.format(
                            "Chunks done: %s/%s", doneChunks, totalChunks
                    ));
                }
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        this.info(String.format(
                "World imported: %s", world
        ));

        return true;
    }

    private Boolean doFetchUuid(String player) throws InterruptedException {
        String stringUuid;
        UUID uuid;

        stringUuid = this.plugin.getApi().getUuidForUsername(player);

        if (stringUuid == null) {
            uuid = Utils.fetchUuid(player);

            if (uuid == null) {
                this.warning(String.format("Unable to fetch UUID for player: %s", player));
                Thread.sleep(1500);  // Mojang rate-limiting..
                return false;
            } else {
                this.plugin.getApi().storePlayer(uuid, player);
                this.info(String.format("Fetched UUID for player: %s", player));
            }

            Thread.sleep(1500);  // Mojang rate-limiting..
        }

        return true;
    }

    private Point2D pointFromFilename(String filename) {
        String[] strings = filename.split("\\.");

        if (strings.length < 3) {
            // Last one is the file extension
            return null;
        }

        return new Point2D(Integer.parseInt(strings[0]), Integer.parseInt(strings[1]));
    }

    private Point3D pointFromStringTuple(String tuple) {
        String[] strings = tuple.split(",");

        if (strings.length < 3) {
            return null;
        }

        return new Point3D(
                Integer.parseInt(strings[0]),
                Integer.parseInt(strings[1]),
                Integer.parseInt(strings[2])
        );
    }

    private Map<Point3D, String> getPointsFromFile(File file, Point2D chunkPoint) {
        if (!file.exists()) {
            return null;
        }

        Map<Point3D, String> points = new HashMap<>();
        FileInputStream in = null;

        try {
            Yaml yaml = new Yaml();
            in = new FileInputStream(file);

            Map<String, Map<String, String>> data =
                    (Map<String, Map<String, String>>) yaml.load(in);

            if (data == null) {
                return null;
            }

            Point3D blockPoint;
            String username;
            String tempUuid;
            Boolean fetched;

            for (Map.Entry<String, Map<String, String>> entry : data.entrySet()) {
                blockPoint = this.pointFromStringTuple(entry.getKey());
                this.translatePointForChunk(chunkPoint, blockPoint);

                username = entry.getValue().get("player");

                if (this.failedUsers.contains(username)) {
                    continue;
                }

                tempUuid = this.plugin.getApi().getUuidForUsername(username);

                if (tempUuid == null) {
                    fetched = this.doFetchUuid(username);

                    if (!fetched) {
                        this.failedUsers.add(username);
                        continue;
                    }

                    tempUuid = this.plugin.getApi().getUuidForUsername(username);
                }

                points.put(blockPoint, tempUuid);
            }
        } catch (FileNotFoundException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return points;
    }

    private void translatePointForChunk(Point2D chunkPoint, Point3D blockPoint) {
        Integer x = chunkPoint.getX() * 16;
        Integer z = chunkPoint.getY() * 16;

        blockPoint.setX(x + blockPoint.getX());
        blockPoint.setZ(z + blockPoint.getZ());
    }

    private void info(String message) {
        this.plugin.getLogger().info(
                String.format("IMPORT | %s", message)
        );
    }

    private void warning(String message) {
        this.plugin.getLogger().warning(
                String.format("IMPORT | %s", message)
        );
    }
}
