package com.bgsoftware.superiorskyblock.utils.reflections;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public final class ReflectionUtils {

    private static SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

    private static Map<Fields, Field> fieldsMap = new HashMap<>();

    static {
        Class<?> chunkProviderClass = getClass("net.minecraft.server.VERSION.ChunkProviderServer"),
                blockFlowerPotClass = getClass("net.minecraft.server.VERSION.BlockFlowerPot"),
                craftInventoryClass = getClass("org.bukkit.craftbukkit.VERSION.inventory.CraftInventory"),
                biomeStorageClass = getClass("net.minecraft.server.VERSION.BiomeStorage"),
                biomeGridClass = getClass("org.bukkit.craftbukkit.VERSION.generator.CustomChunkGenerator$CustomBiomeGrid");

        if(ServerVersion.isAtLeast(ServerVersion.v1_15)){
            fieldsMap.put(Fields.BIOME_GRID_BIOME_STORAGE, getField(biomeGridClass, "biome"));
            Field field = getField(biomeStorageClass, "f");
            if(field != null && !field.getType().getName().contains("BiomeBase"))
                field = getField(biomeStorageClass, "g");
            fieldsMap.put(Fields.BIOME_STORAGE_BIOME_BASES, field);
        }

        if(ServerVersion.isAtLeast(ServerVersion.v1_13)) {
            fieldsMap.put(Fields.BLOCK_FLOWER_POT_CONTENT, getField(blockFlowerPotClass, "c"));
            fieldsMap.put(Fields.CRAFT_INVENTORY_INVENTORY, getField(craftInventoryClass, "inventory"));
        }

        if(ServerVersion.isEquals(ServerVersion.v1_8)) {
            try {
                fieldsMap.put(Fields.CHUNK_PROVIDER_UNLOAD_QUEUE, getField(chunkProviderClass, "unloadQueue"));
            } catch (Exception ignored) {
            }
        }

    }

    public static Class<?> getClass(String classPath){
        try {
            return Class.forName(classPath.replace("VERSION", version));
        } catch(ClassNotFoundException ex){
            throw new NullPointerException(ex.getMessage());
        }
    }

    public static Constructor<?> getConstructor(Class<?> clazz, Class<?>... parameterTypes){
        try{
            return clazz.getConstructor(parameterTypes);
        }catch(NoSuchMethodException ex){
            return null;
        }
    }

    public static Constructor<?> getDeclaredConstructor(Class<?> clazz, Class<?>... parameterTypes){
        try{
            Constructor<?> constructor = clazz.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);
            return constructor;
        }catch(NoSuchMethodException ex){
            return null;
        }
    }

    public static Method getMethod(Class<?> clazz, String name, Class<?> returnType, Class<?>... parameterTypes){
        try{
            Method method = clazz.getMethod(name, parameterTypes);
            return returnType == null || method.getReturnType().equals(returnType) ? method : null;
        }catch(NoSuchMethodException ex){
            return null;
        }
    }

    static Field getFromFields(Fields fields){
        return fieldsMap.get(fields);
    }

    private static Field getField(Class<?> clazz, String fieldName){
        try{
            Field field =  clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        }catch(Exception ex){
            SuperiorSkyblockPlugin.log("&cCouldn't find the field " + fieldName + " - Please contact Ome_R!");
            Executor.sync(() -> Bukkit.getPluginManager().disablePlugin(plugin));
            return null;
        }
    }

}
