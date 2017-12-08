# DynamicViewDistance
Dynamic per-player view distances - this plugin utilises Mixins, be warned!

## Commands
| Command | Permission | Default Role  | Description |
| :------ | :--------- | :-----------  | :---------- |
| `dynview reload` | `dynview.admin` |   | Reloads the config and refreshes all users |
| `dynview reset` | `dynview.custom` |   | Resets your server-side view distance to the default |
| `dynview set <distance>` | `dynview.custom` |   | Sets your server-side view distance to a custom value |
| `dynview test <target>` | `dynview.admin` |   | Check what server-side view distance the target player has been set |

## Config
#### Format:
```
<world#0;string>: {
    <threshold#0;int>: {
        <group#0_name;string>: <distance;int>
        <group#1_name;string>: <distance;int>
        ...
    }
    <threshold#1;int>: {
        ...
    }
}
```
##### Parameters:
- `<world>` - the name of the world where the encapsulated rules will take effect.
- `<threshold>` - the minimum number of players online for the encapsulated view distances to take effect.
- `<group>` - the name name of the group that the `<distance>` applies to.
- `<distance>` - the view distance in chunks (this is actually the radius)

##### Notes:
- To create a global set of rules, use the name 'global' instead of a world name.
- You can set a default view distance in each threshold band by using 'default' as the `<group>` name.
- Players with the permission `dynview.group.<group>` will be considered in that group.
- The largest view distance will be used if a player is in more than one applicable group.
- Players with the permission `dynview.bypass` will bypass all rules.

#### Example:
```
global: {
    0: {
        default: 16
    }
    25: {
        default: 10
    }
    50: {
        default: 6
    }
}
my_world: {
    0: {
        default: 4
    }
}
```
##### Explained:
- player view distances will be reduced when there are +25 players online, and reduced further with +50 online.
- any players in the world 'my_world' will be limited to 4 chunks regardless of the number of players online.
