package net.milkbowl.vault;

import java.util.Collection;
import java.util.logging.Logger;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;

import org.bukkit.command.CommandSender;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Vault extends JavaPlugin { 
   private static Logger log;
   private Permission perms;
   private String currentVersionTitle = "";
   
   private ServicesManager sm;
   
   private Vault plugin;
   
   public void onDisable() {
     getServer().getServicesManager().unregisterAll(this);
     Bukkit.getScheduler().cancelTasks(this);
   }
 
   
   public void onEnable() {
     this.plugin = this;
     log = getLogger();
     this.currentVersionTitle = getDescription().getVersion().split("-")[0];
     this.sm = getServer().getServicesManager();
     
     getCommand("vault-info").setExecutor(this);
     getCommand("vault-convert").setExecutor(this);
     
     log.info(String.format("Enabled Version %s", getDescription().getVersion()));
   }


   private void hookChat(String name, Class<? extends Chat> hookClass, ServicePriority priority, String... packages) {
     try {
       if (packagesExists(packages)) {
         Chat chat = hookClass.getConstructor(new Class[] { Plugin.class, Permission.class }).newInstance(this, this.perms);
         this.sm.register(Chat.class, chat, this, priority);
         log.info(String.format("[Chat] %s found: %s", name, chat.isEnabled() ? "Loaded" : "Waiting"));
       } 
     } catch (Exception e) {
       log.severe(String.format("[Chat] There was an error hooking %s - check to make sure you're using a compatible version!", name));
     } 
   }


   private void hookEconomy(String name, Class<? extends Economy> hookClass, ServicePriority priority, String... packages) {
     try {
       if (packagesExists(packages)) {
         Economy econ = hookClass.getConstructor(new Class[] { Plugin.class }).newInstance(this);
         this.sm.register(Economy.class, econ, this, priority);
         log.info(String.format("[Economy] %s found: %s", name, econ.isEnabled() ? "Loaded" : "Waiting"));
       } 
     } catch (Exception e) {
       log.severe(String.format("[Economy] There was an error hooking %s - check to make sure you're using a compatible version!", name));
     } 
   }


   private void hookPermission(String name, Class<? extends Permission> hookClass, ServicePriority priority, String... packages) {
     try {
       if (packagesExists(packages)) {
         Permission perms = hookClass.getConstructor(new Class[] { Plugin.class }).newInstance(this);
         this.sm.register(Permission.class, perms, this, priority);
         log.info(String.format("[Permission] %s found: %s", name, perms.isEnabled() ? "Loaded" : "Waiting"));
       } 
     } catch (Exception e) {
       log.severe(String.format("[Permission] There was an error hooking %s - check to make sure you're using a compatible version!", name));
     } 
   }
 
   
   public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
     if (!sender.hasPermission("vault.admin")) {
       sender.sendMessage("You do not have permission to use that command!");
       return true;
     } 
     
     if (command.getName().equalsIgnoreCase("vault-info")) {
       infoCommand(sender);
       return true;
     }  if (command.getName().equalsIgnoreCase("vault-convert")) {
       convertCommand(sender, args);
       return true;
     } 
     
     sender.sendMessage("Vault Commands:");
     sender.sendMessage("  /vault-info - Displays information about Vault");
     sender.sendMessage("  /vault-convert [economy1] [economy2] - Converts from one Economy to another");
     return true;
   }
 
   
   private void convertCommand(CommandSender sender, String[] args) {
     Collection<RegisteredServiceProvider<Economy>> econs = getServer().getServicesManager().getRegistrations(Economy.class);
     if (econs == null || econs.size() < 2) {
       sender.sendMessage("You must have at least 2 economies loaded to convert."); return;
     } 
     if (args.length != 2) {
       sender.sendMessage("You must specify only the economy to convert from and the economy to convert to. (names should not contain spaces)");
       return;
     } 
     Economy econ1 = null;
     Economy econ2 = null;
     String economies = "";
     for (RegisteredServiceProvider<Economy> econ : econs) {
       String econName = econ.getProvider().getName().replace(" ", "");
       if (econName.equalsIgnoreCase(args[0])) {
         econ1 = econ.getProvider();
       } else if (econName.equalsIgnoreCase(args[1])) {
         econ2 = econ.getProvider();
       } 
       if (economies.length() > 0) {
         economies = economies + ", ";
       }
       economies = economies + econName;
     } 
     
     if (econ1 == null) {
       sender.sendMessage("Could not find " + args[0] + " loaded on the server, check your spelling.");
       sender.sendMessage("Valid economies are: " + economies); return;
     } 
     if (econ2 == null) {
       sender.sendMessage("Could not find " + args[1] + " loaded on the server, check your spelling.");
       sender.sendMessage("Valid economies are: " + economies);
       
       return;
     } 
     sender.sendMessage("This may take some time to convert, expect server lag.");
     for (OfflinePlayer op : Bukkit.getServer().getOfflinePlayers()) {
       if (econ1.hasAccount(op) && 
         !econ2.hasAccount(op)) {
 
         
         econ2.createPlayerAccount(op);
         double diff = econ1.getBalance(op) - econ2.getBalance(op);
         if (diff > 0.0D) {
           econ2.depositPlayer(op, diff);
         } else if (diff < 0.0D) {
           econ2.withdrawPlayer(op, -diff);
         } 
       } 
     } 
     
     sender.sendMessage("Converson complete, please verify the data before using it.");
   }
 
   
   private void infoCommand(CommandSender sender) {
     String registeredEcons = null;
     Collection<RegisteredServiceProvider<Economy>> econs = getServer().getServicesManager().getRegistrations(Economy.class);
     for (RegisteredServiceProvider<Economy> registeredServiceProvider : econs) {
       Economy e = registeredServiceProvider.getProvider();
       if (registeredEcons == null) {
         registeredEcons = e.getName(); continue;
       } 
       registeredEcons = registeredEcons + ", " + e.getName();
     } 


     String registeredPerms = null;
     Collection<RegisteredServiceProvider<Permission>> perms = getServer().getServicesManager().getRegistrations(Permission.class);
     for (RegisteredServiceProvider<Permission> registeredServiceProvider : perms) {
       Permission p = registeredServiceProvider.getProvider();
       if (registeredPerms == null) {
         registeredPerms = p.getName(); continue;
       } 
       registeredPerms = registeredPerms + ", " + p.getName();
     } 
 
     
     String registeredChats = null;
     Collection<RegisteredServiceProvider<Chat>> chats = getServer().getServicesManager().getRegistrations(Chat.class);
     for (RegisteredServiceProvider<Chat> registeredServiceProvider : chats) {
       Chat c = registeredServiceProvider.getProvider();
       if (registeredChats == null) {
         registeredChats = c.getName(); continue;
       } 
       registeredChats = registeredChats + ", " + c.getName();
     } 
 
 
     
     RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
     Economy econ = null;
     if (rsp != null) {
       econ = rsp.getProvider();
     }
     Permission perm = null;
     RegisteredServiceProvider<Permission> rspp = getServer().getServicesManager().getRegistration(Permission.class);
     if (rspp != null) {
       perm = rspp.getProvider();
     }
     Chat chat = null;
     RegisteredServiceProvider<Chat> rspc = getServer().getServicesManager().getRegistration(Chat.class);
     if (rspc != null) {
       chat = rspc.getProvider();
     }
     
     sender.sendMessage(String.format("[%s] Vault v%s Information", getDescription().getName(), getDescription().getVersion()));
     sender.sendMessage(String.format("[%s] Economy: %s [%s]", getDescription().getName(), (econ == null) ? "None" : econ.getName(), registeredEcons));
     sender.sendMessage(String.format("[%s] Permission: %s [%s]", getDescription().getName(), (perm == null) ? "None" : perm.getName(), registeredPerms));
     sender.sendMessage(String.format("[%s] Chat: %s [%s]", getDescription().getName(), (chat == null) ? "None" : chat.getName(), registeredChats));
   }
   
   private static boolean packagesExists(String... packages) {
     try {
       for (String pkg : packages) {
         Class.forName(pkg);
       }
       return true;
     } catch (Exception e) {
       return false;
     } 
   }
 }