package mr.minecraft15.ticket;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import mr.minecraft15.ticket.commands.TicketCommand;
import mr.minecraft15.ticket.listeners.PlayerJoin;
import mr.minecraft15.ticket.utils.MessageManager;
import mr.minecraft15.ticket.utils.TicketManager;
import mr.minecraft15.ticket.utils.Utils;

public class Main extends JavaPlugin {
    private static Main instance;
    private static MessageManager messageManager;
    private static TicketManager ticketManager;
    private static int maxTickets;

    @Override
    public void onEnable() {
	setInstance(this);

	final FileConfiguration cfg = getConfig();
	cfg.options().copyDefaults(true);

	cfg.addDefault("Ticket.Maximum_Per_User", 3);

	cfg.addDefault("MySQL.Hostname", "localhost");
	cfg.addDefault("MySQL.Port", 3306);
	cfg.addDefault("MySQL.Database", "Ticket");
	cfg.addDefault("MySQL.Username", "svcTicket");
	cfg.addDefault("MySQL.Password", "T1(k3t");

	cfg.addDefault("Messages.Prefix", "&8[&bTicket&8]&7");
	cfg.addDefault("Messages.Permission", "ticket.admin");

	cfg.addDefault("Messages.Max_Tickets_Exceeded",
		"%prefix% You already created the maximum amount of tickets. Please wait until all your tickets are solved, before you create new ones.");
	cfg.addDefault("Messages.Ticket_Closed",
		"%prefix% The ticket &b#%id% &7was closed by &b%supporter%&7. &b%answer%");
	cfg.addDefault("Messages.Please_Specify_Request",
		"%prefix% Please describe your problem behind the command. A staff will help you as soon as possible.");
	cfg.addDefault("Messages.Open_Tickets", "%prefix% Open tickets:");
	cfg.addDefault("Messages.Ticket_Not_Existing", "%prefix% The ticket &b#%id% &7does not exist.");
	cfg.addDefault("Messages.No_Permission", "%prefix% You don't have enough permissions to do this.");
	cfg.addDefault("Messages.No_Supporter_Assigned", "%prefix% Nobody is assigned to the ticket &b#%id%&7.");
	cfg.addDefault("Messages.Already_Assigned", "%prefix% You are already assigned to ticket &b#%id%&7.");
	cfg.addDefault("Messages.No_Open_Tickets", "%prefix% There are no open tickets.");
	cfg.addDefault("Messages.Admin_Menu",
		Arrays.asList(new String[] { "&b/Ticket <tp> <ticket id> &7- Teleports you to a ticket",
			"&b/Ticket <assign> <ticket id> &7- Assigns you to a ticket",
			"&b/Ticket <unassign> <ticket id> &7- Unassigns you from a ticket",
			"&b/Ticket <auto> <ticket id> &7- Teleports you to the ticket and assigns it to you",
			"&b/Ticket <next> &7- Teleports you to the next ticket and assigns it to you" }));
	cfg.addDefault("Messages.Menu", Arrays.asList(new String[] {
		"&b/Ticket <new|create> <your problem> &7- Creates a new ticket",
		"&b/Ticket <list> &7- Shows all open tickets",
		"&b/Ticket <close> <ticket id> [reason] &7- Closes the ticket", "",
		"&c&lImportant: &cPlase note, that you have to create the ticket at the place where your problem is located. Otherwise the staffs won't be able to locate your problem." }));
	cfg.addDefault("Messages.Open_Ticket_Amount",
		"%prefix% Open tickets: &b%amount% &7(&b%amount_without_supporter% &7unassigned)");
	cfg.addDefault("Messages.Please_Specify_Ticket_Id", "%prefix% Please specify a ticket id.");
	cfg.addDefault("Messages.No_Supporter", "Unassigned");
	cfg.addDefault("Messages.List_Ticket_With_Assign_Option",
		Arrays.asList(new String[] { "%prefix% Ticket &b#%id% &7from &b%creator%&7:",
			"[assign]  &7Supporter: &b%supporter% &7&n(Assign)",
			"[teleport]  &7Request: &b%request% &7&n(Teleport)" }));
	cfg.addDefault("Messages.List_Ticket_With_Unassign_Option",
		Arrays.asList(new String[] { "%prefix% Ticket &b#%id% &7from &b%creator%&7:",
			"[unassign]  &7Supporter: &b%supporter% &7&n(Unassign)",
			"[teleport]  &7Request: &b%request% &7&n(Teleport)" }));
	cfg.addDefault("Messages.List_Own_Ticket",
		Arrays.asList(new String[] { "  &7Ticket &b#%id%&7: &b%request%", "  &7Supporter: &b%supporter%" }));
	cfg.addDefault("Messages.Teleported_To_Position",
		"%prefix% You were teleported the the position of ticket &b#%id%&7.");
	cfg.addDefault("Messages.Teleporting_To_Server",
		"%prefix% Teleporting to server of ticket &b#%id% &7... (You may want to teleport to the position of the ticket &b#%id% &7on the server &b%server% &7with &b/Ticket tp %id%&7.)");
	cfg.addDefault("Messages.Ticket_Assigned", "%prefix% The ticket &b#%id% &7got assigned to &b%supporter%&7.");
	cfg.addDefault("Messages.Ticket_Unassigned",
		"%prefix% The ticket &b#%id% &7got unassigned from &b%supporter%&7.");
	cfg.addDefault("Messages.Unable_To_Create_Ticket",
		"%prefix% The ticket couldn't be created. Please try again later.");
	cfg.addDefault("Messages.Ticket_Created",
		"%prefix% The ticket &b#%id% &7was successfully created. A staff will help you as soon as possible.");
	cfg.addDefault("Messages.Ticket_Created_Info",
		"%prefix% &b%creator% &7has created the ticket &b#%id% &7(&b%tickets%&7/&b%max_tickets%&7): &b%request%");

	// cfg.addDefault("Messages.Max_Tickets_Exceeded",
	// "%prefix% Du hast bereits die maximale Anzahl an Tickets erstellt. Bitte
	// warte bis diese bearbeitet wurden bevor du neue Tickets erstellst.");
	// cfg.addDefault("Messages.Ticket_Closed",
	// "%prefix% Das Ticket &b#%id% &7wurde von &b%supporter% &7geschlossen.
	// &b%answer%");
	// cfg.addDefault("Messages.Please_Specify_Request",
	// "%prefix% Bitte beschreibe hinter dem Befehl dein Anliegen. Ein Teammitglied
	// wird sich dann umgehend darum kümmern.");
	// cfg.addDefault("Messages.Open_Tickets", "%prefix% Offene Tickets:");
	// cfg.addDefault("Messages.Ticket_Not_Existing", "%prefix% Das Ticket &b#%id%
	// &7existiert nicht.");
	// cfg.addDefault("Messages.No_Permission", "%prefix% Du hast dafür keine
	// Erlaubnis.");
	// cfg.addDefault("Messages.No_Supporter_Assigned", "%prefix% Es ist niemand für
	// das Ticket &b#%id% &7zugewiesen.");
	// cfg.addDefault("Messages.Already_Assigned", "%prefix% Du bist bereits Ticket
	// &b#%id% &7zugewiesen.");
	// cfg.addDefault("Messages.No_Open_Tickets", "%prefix% Es sind keine Tickets
	// offen.");
	// cfg.addDefault("Messages.Admin_Menu",
	// Arrays.asList(new String[] { "&b/Ticket <zuweisen> <Ticketnummer> &7- Weist
	// dir das Ticket zu",
	// "&b/Ticket <abweisen> <Ticketnummer> &7- Weist das Ticket ab",
	// "&b/Ticket <auto> <Ticketnummer> &7- Teleportiert dich zum Ticket und weist
	// es dir zu",
	// "&b/Ticket <next> &7- Teleportiert dich zum nächsten Ticket und weist es dir
	// zu" }));
	// cfg.addDefault("Messages.Menu", Arrays.asList(new String[] {
	// "&b/Ticket <neu|erstellen> <Dein Anliegen> &7- Öffnet ein neues Ticket",
	// "&b/Ticket <Liste> &7- Zeigt alle offenen Tickets an",
	// "&b/Ticket <tp> <Ticketnummer> &7- Teleportiert dich zum Ticket",
	// "&b/Ticket <schließen> <Ticketnummer> [Grund] &7- Schließt das Ticket", "",
	// "&c&lWichtig: &cBitte beachte, dass du an dem Ort stehen musst, wo sich dein
	// Anliegen befindet, wenn du ein Ticket erstellst." }));
	// cfg.addDefault("Messages.Open_Ticket_Amount",
	// "%prefix% Offene Tickets: &b%amount% &7(&b%amount_without_supporter% &7ohne
	// Bearbeiter)");
	// cfg.addDefault("Messages.Please_Specify_Ticket_Id", "%prefix% Bitte gebe eine
	// Ticketnummer an.");
	// cfg.addDefault("Messages.No_Supporter", "Kein Bearbeiter");
	// cfg.addDefault("Messages.List_Ticket_With_Assign_Option",
	// Arrays.asList(new String[] { "%prefix% Ticket &b#%id% &7von &b%creator%&7:",
	// "[assign] &7Bearbeiter: &b%supporter% &7&n(Zuweisen)",
	// "[teleport] &7Anliegen: &b%request% &7&n(Teleportieren)" }));
	// cfg.addDefault("Messages.List_Ticket_With_Unassign_Option",
	// Arrays.asList(new String[] { "%prefix% Ticket &b#%id% &7von &b%creator%&7:",
	// "[unassign] &7Bearbeiter: &b%supporter% &7&n(Abweisen)",
	// "[teleport] &7Anliegen: &b%request% &7&n(Teleportieren)" }));
	// cfg.addDefault("Messages.List_Own_Ticket",
	// Arrays.asList(new String[] { " &7Ticket &b#%id%&7: &b%request%", "
	// &7Bearbeiter: &b%supporter%" }));
	// cfg.addDefault("Messages.Teleported_To_Position",
	// "%prefix% Du wurdest zu der Position von Ticket &b#%id% &7teleportiert.");
	// cfg.addDefault("Messages.Teleporting_To_Server",
	// "%prefix% Du wirst auf den Server von Ticket &b#%id% &7teleportiert... (Du
	// musst dich gegebenenfalls noch zu der Position des Tickets auf dem Server
	// &b%server% &7teleportieren.)");
	// cfg.addDefault("Messages.Ticket_Assigned", "%prefix% Das Ticket &b#%id%
	// &7wurde &b%supporter% &7zugewiesen.");
	// cfg.addDefault("Messages.Ticket_Unassigned", "%prefix% Das Ticket &b#%id%
	// &7wurde &b%supporter% &7abgewiesen.");
	// cfg.addDefault("Messages.Unable_To_Create_Ticket",
	// "%prefix% Das Ticket konnte nicht erstellt werden. Bitte versuche es später
	// erneut.");
	// cfg.addDefault("Messages.Ticket_Created",
	// "%prefix% Das Ticket &b#%id% &7wurde erfolgreich erstellt. Ein Teammitglied
	// wird sich dann umgehend darum kümmern.");
	// cfg.addDefault("Messages.Ticket_Created_Info",
	// "%prefix% &b%creator% &7hat das Ticket &b#%id% &7erstellt
	// (&b%tickets%&7/&b%max_tickets%&7): &b%request%");

	saveConfig();

	setMaxTickets(cfg.getInt("Ticket.Maximum_Per_User"));

	setMessageManager(new MessageManager(cfg));

	setTicketManager(new TicketManager(cfg.getString("MySQL.Hostname"), cfg.getInt("MySQL.Port"),
		cfg.getString("MySQL.Database"), cfg.getString("MySQL.Username"), cfg.getString("MySQL.Password")));

	getCommand("ticket").setExecutor(new TicketCommand());

	Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

	Bukkit.getPluginManager().registerEvents(new PlayerJoin(), this);

	Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
	    @Override
	    public void run() {
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
		    if (player.hasPermission(messageManager.getMessage("Permission"))) {
			Utils.sendOpenTickets(player);
		    }
		}
	    }
	}, 0, 20 * 300);
    }

    public static Main getInstance() {
	return instance;
    }

    public static void setInstance(Main instance) {
	Main.instance = instance;
    }

    public static MessageManager getMessageManager() {
	return messageManager;
    }

    public static void setMessageManager(MessageManager messageManager) {
	Main.messageManager = messageManager;
    }

    public static TicketManager getTicketManager() {
	return ticketManager;
    }

    public static void setTicketManager(TicketManager ticketManager) {
	Main.ticketManager = ticketManager;
    }

    public static int getMaxTickets() {
	return maxTickets;
    }

    public static void setMaxTickets(int maxTickets) {
	Main.maxTickets = maxTickets;
    }
}
