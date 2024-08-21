# VaultMapper
A mod that adds an on-screen minimap of the vault you're in for the Vault Hunters 3rd Edition modpack.

### Planned Features (todo)
- [ ] Add an option to rotate the map based on the vault's facing direction or the player's facing direction
- [x] Grab the inscription data from the hologram block and display it on the map
 - [ ] Show the inscription icons on the map
- [ ] Find a way to get room types (classic rooms, ore rooms, challenge rooms, omega rooms, "cut" rooms - no clue what those even are) and show them on the map
- [ ] Lock the map behind a research or an item or something like that
> "If this is implemented, it will probably need a research beyond Vault Compass, and a crafting item that is significantly more expensive (costing a couple POGs probably)" - @rmzing

> `boolean hasCompassResearch = StageManager.getResearchTree(Minecraft.getInstance().player).getResearchesDone().contains("Vault Compass");` - @josephfranci
- [ ] Detect edges of the vault and show them on the map
- [ ] Add a way to sync map data between players
- [ ] Add a way to make certain "important" rooms show differently on the map
> "Waypoint support would still be cool, like waypoints displaying on ur map or rooms changing color if a waypoint is in there" - @josephfranci

> "Or maybe use the vault compass? The same way as how holding right click on the compass changes where it points to, if the vault compass points to a room that isn’t the start then the minimap points to that room" - @emodigestive

### Known Issues
- Tunnels are partly drawn over the start room when the vault is facing east or south
- The map is not automatically enabled when entering a vault on a server - you can enable it manually by using the `/vaultmapper enable` command