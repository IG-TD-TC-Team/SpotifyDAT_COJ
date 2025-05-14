package spotifyServer.commandProcessor;

/**
 * HelpCommandProcessor that provides comprehensive help information for all available commands.
 * This processor serves as the documentation hub for the Spotify Server, organizing commands
 * by category for better user experience.
 */
public class HelpCommandProcessor extends AbstractProcessor {
    @Override
    public String processCommand(String command) {
        if (command.equalsIgnoreCase("help")) {
            // Create a comprehensive help message with all available commands
            StringBuilder help = new StringBuilder();
            help.append("╔════════════════════════════════════════════════════════════════════╗\n");
            help.append("║                  SPOTIFY SERVER COMMAND REFERENCE                  ║\n");
            help.append("╚════════════════════════════════════════════════════════════════════╝\n\n");

            // Basic System Commands
            help.append("SYSTEM COMMANDS:\n");
            help.append("═══════════════\n");
            help.append("  help                    - Show this comprehensive help message\n");
            help.append("  exit                    - Disconnect from the server\n\n");

            // Authentication Commands
            help.append("AUTHENTICATION:\n");
            help.append("═══════════════\n");
            help.append("  login <username> <password>    - Authenticate with your credentials\n");
            help.append("  logout                         - End your current session\n");
            help.append("  register <username> <email> <password> <firstName> <lastName>\n");
            help.append("                                - Create a new account\n\n");

            // Profile Management Commands
            help.append("PROFILE MANAGEMENT:\n");
            help.append("═══════════════════\n");
            help.append("  viewprofile                           - Display your profile information\n");
            help.append("  updateprofile <field> <newValue>      - Update profile information\n");
            help.append("                                         Fields: firstname, lastname, email\n");
            help.append("  changepassword <current> <new>        - Change your password\n\n");

            // Social Features Commands
            help.append("SOCIAL FEATURES:\n");
            help.append("════════════════\n");
            help.append("  follow id <userId>              - Follow a user by their ID\n");
            help.append("  follow username <username>      - Follow a user by their username\n");
            help.append("  unfollow id <userId>            - Unfollow a user by their ID\n");
            help.append("  unfollow username <username>    - Unfollow a user by their username\n");
            help.append("  followers                       - List all your followers\n");
            help.append("  following                       - List all users you follow\n");
            help.append("  searchuser name <name>          - Search users by name\n");
            help.append("  searchuser username <username>  - Search users by username\n\n");

            // Subscription Commands
            help.append("SUBSCRIPTION MANAGEMENT:\n");
            help.append("════════════════════════\n");
            help.append("  viewsubscription                - View your current subscription details\n");
            help.append("  upgradesubscription <plan>      - Upgrade your subscription\n");
            help.append("                                   Plans: premium, family, student\n");
            help.append("  downgradesubscription           - Downgrade to free subscription\n\n");

            // Playlist Management Commands
            help.append("PLAYLIST MANAGEMENT:\n");
            help.append("════════════════════\n");
            help.append("  createplaylist <name>                  - Create a new playlist\n");
            help.append("  renameplaylist id <id> <newName>       - Rename playlist by ID\n");
            help.append("  renameplaylist name <old> <new>        - Rename playlist by name\n");
            help.append("  deleteplaylist id <id>                 - Delete playlist by ID\n");
            help.append("  deleteplaylist name <name>             - Delete playlist by name\n");
            help.append("  viewplaylist id <id>                   - View playlist details by ID\n");
            help.append("  viewplaylist name <name>               - View playlist details by name\n");
            help.append("  listplaylists                          - List all your playlists\n");
            help.append("  addtoplaylist playlistid <pid> songid <sid>\n");
            help.append("                                        - Add a song to a playlist\n");
            help.append("  removefromplaylist playlistid <pid> songid <sid>\n");
            help.append("                                        - Remove a song from a playlist\n\n");

            // Playlist Social Features
            help.append("PLAYLIST SOCIAL FEATURES:\n");
            help.append("═════════════════════════\n");
            help.append("  shareplaylist playlistid <pid> userid <uid>\n");
            help.append("                                        - Share a playlist with a user\n");
            help.append("  unshareplaylist playlistid <pid> userid <uid>\n");
            help.append("                                        - Unshare a playlist with a user\n");
            help.append("  likeplaylist id <id>                  - Like a playlist by ID\n");
            help.append("  likeplaylist name <name>              - Like a playlist by name\n");
            help.append("  unlikeplaylist id <id>                - Unlike a playlist by ID\n");
            help.append("  unlikeplaylist name <name>            - Unlike a playlist by name\n");
            help.append("  viewlikedplaylists                    - View all playlists you've liked\n");
            help.append("  viewplaylistlikes [id/name] [value]   - View likes on your playlists\n\n");

            // Music Playback Commands
            help.append("MUSIC PLAYBACK:\n");
            help.append("═══════════════\n");
            help.append("  play <song_id>          - Stream a specific song by its ID\n");
            help.append("  playlist <playlist_id>  - Stream an entire playlist by its ID\n\n");

            // Music Search and Browse Commands
            help.append("SEARCH & BROWSE:\n");
            help.append("════════════════\n");
            help.append("  search <query>                    - Search for songs by title\n");
            help.append("  listsongs artist id <id>          - List songs by artist ID\n");
            help.append("  listsongs artist name <name>      - List songs by artist name\n");
            help.append("  listsongs album id <id>           - List songs by album ID\n");
            help.append("  listsongs album name <name>       - List songs by album name\n");
            help.append("  listsongs genre <genre>           - List songs by genre\n");
            help.append("      Genres: ROCK, POP, JAZZ, CLASSICAL, HIP_HOP, ELECTRONIC, COUNTRY, BLUES\n\n");

            help.append("╔════════════════════════════════════════════════════════════════════╗\n");
            help.append("║ TIP: Commands are case-insensitive. Type 'exit' to disconnect.     ║\n");
            help.append("╚════════════════════════════════════════════════════════════════════╝\n");

            return help.toString();
        }

        // Pass to next processor if this one can't handle it
        return handleNext(command);
    }
}