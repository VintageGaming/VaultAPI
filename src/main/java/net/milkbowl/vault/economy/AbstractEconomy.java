package net.milkbowl.vault.economy;

import org.bukkit.OfflinePlayer;


public abstract class AbstractEconomy
  implements Economy
{
  public boolean hasAccount(OfflinePlayer player) {
     if (player.getName() == null) return false; 
     return hasAccount(player.getName());
  }

  
  public boolean hasAccount(OfflinePlayer player, String worldName) {
     if (player.getName() == null) return false; 
     return hasAccount(player.getName(), worldName);
  }

  
  public double getBalance(OfflinePlayer player) {
     return getBalance(player.getName());
  }

  
  public double getBalance(OfflinePlayer player, String world) {
     return getBalance(player.getName(), world);
  }

  
  public boolean has(OfflinePlayer player, double amount) {
     if (player.getName() == null) return false; 
     return has(player.getName(), amount);
  }

  
  public boolean has(OfflinePlayer player, String worldName, double amount) {
     if (player.getName() == null) return false; 
     return has(player.getName(), worldName, amount);
  }

  
  public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
     return withdrawPlayer(player.getName(), amount);
  }

  
  public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
     return withdrawPlayer(player.getName(), worldName, amount);
  }

  
  public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
     return depositPlayer(player.getName(), amount);
  }

  
  public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
     return depositPlayer(player.getName(), worldName, amount);
  }

  
  public EconomyResponse createBank(String name, OfflinePlayer player) {
     return createBank(name, player.getName());
  }

  
  public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
     return isBankOwner(name, player.getName());
  }

  
  public EconomyResponse isBankMember(String name, OfflinePlayer player) {
     return isBankMember(name, player.getName());
  }

  
  public boolean createPlayerAccount(OfflinePlayer player) {
     return createPlayerAccount(player.getName());
  }

  
  public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
     return createPlayerAccount(player.getName(), worldName);
  }
}