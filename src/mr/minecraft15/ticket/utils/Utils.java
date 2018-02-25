package mr.minecraft15.ticket.utils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import mr.minecraft15.ticket.Main;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;

public class Utils {
    public static void sendOpenTickets(Player player) {
	int openTickets = Main.getTicketManager().getOpenTickets().size();
	if (openTickets > 0) {
	    player.sendMessage(Main.getMessageManager().getMessage("Open_Ticket_Amount", "amount", openTickets,
		    "amount_without_supporter", Main.getTicketManager().getOpenTicketsWithoutSupporter().size()));
	}
    }

    public static void listTickets(Player p) {
	MessageManager messageManager = Main.getMessageManager();
	TicketManager ticketManager = Main.getTicketManager();

	List<Ticket> openTickets = ticketManager.getOpenTickets();

	if (openTickets.isEmpty()) {
	    p.sendMessage(messageManager.getMessage("No_Open_Tickets"));
	} else {
	    p.sendMessage(messageManager.getMessage("Open_Tickets"));
	    for (Ticket ticket : openTickets) {
		if (p.getUniqueId().equals(ticket.getSupporter())) {
		    Utils.listTicketMessage(p, ticket, "List_Ticket_With_Unassign_Option");
		} else {
		    Utils.listTicketMessage(p, ticket, "List_Ticket_With_Assign_Option");
		}
	    }
	}
    }

    public static void listTicketMessage(Player p, Ticket ticket, String messageKey) {
	MessageManager messageManager = Main.getMessageManager();
	for (String message : messageManager
		.getList(messageKey, "id", ticket.getId(), "creator", ticket.getCreatorName(), "supporter",
			ticket.getSupporter() == null ? messageManager.getMessage("No_Supporter")
				: Bukkit.getOfflinePlayer(ticket.getSupporter()).getName(),
			"request", ticket.getRequest())) {
	    if (message.startsWith("[assign]")) {
		message = message.replace("[assign]", "");
		TextComponent textComponent = new TextComponent();
		textComponent.setText(message);
		textComponent.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/ticket assign " + ticket.getId()));
		p.spigot().sendMessage(textComponent);
	    } else if (message.startsWith("[unassign]")) {
		message = message.replace("[unassign]", "");
		TextComponent textComponent = new TextComponent();
		textComponent.setText(message);
		textComponent.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/ticket unassign " + ticket.getId()));
		p.spigot().sendMessage(textComponent);
	    } else if (message.startsWith("[teleport]")) {
		message = message.replace("[teleport]", "");
		TextComponent textComponent = new TextComponent();
		textComponent.setText(message);
		textComponent.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/ticket teleport " + ticket.getId()));
		p.spigot().sendMessage(textComponent);
	    } else {
		p.sendMessage(message);
	    }
	}
    }

    public static void listOwnTickets(Player p) {
	MessageManager messageManager = Main.getMessageManager();
	TicketManager ticketManager = Main.getTicketManager();

	List<Ticket> ownTickets = ticketManager.getTickets(p);

	if (ownTickets.isEmpty()) {
	    p.sendMessage(messageManager.getMessage("No_Open_Tickets"));
	} else {
	    p.sendMessage(messageManager.getMessage("Open_Tickets"));
	    for (Ticket ticket : ownTickets) {
		for (String message : messageManager.getList("List_Own_Ticket", "id", ticket.getId(), "request",
			ticket.getRequest(), "supporter", ticket.getSupporterName())) {
		    p.sendMessage(message);
		}
	    }
	}
    }

    public static void notExisting(Player p, String ticketId) {
	p.sendMessage(Main.getMessageManager().getMessage("Ticket_Not_Existing", "id", ticketId));
    }

    public static void teleportToTicket(Player p, Ticket ticket) {
	MessageManager messageManager = Main.getMessageManager();
	if (Bukkit.getServerName().equals(ticket.getServer())) {
	    p.teleport(ticket.getLocation());
	    p.sendMessage(messageManager.getMessage("Teleported_To_Position", "id", ticket.getId()));
	} else {
	    p.sendMessage(messageManager.getMessage("Teleporting_To_Server", "id", ticket.getId(), "server",
		    ticket.getServer()));
	    Utils.sendToServer(p, ticket.getServer());
	}
    }

    public static void noPermission(Player p) {
	p.sendMessage(Main.getMessageManager().getMessage("No_Permission"));
    }

    public static void addTicketNumber(Player p) {
	p.sendMessage(Main.getMessageManager().getMessage("Please_Specify_Ticket_Id"));
    }

    public static void assignTicket(Player p, Ticket ticket) {
	ticket.setSupporter(p.getUniqueId());
	OfflinePlayer pt = Bukkit.getServer().getOfflinePlayer(ticket.getCreator());
	String message = Main.getMessageManager().getMessage("Ticket_Assigned", "id", ticket.getId(), "supporter",
		ticket.getSupporterName());
	if (pt.isOnline()) {
	    ((CommandSender) pt).sendMessage(message);
	}
	Utils.teamInfo(message);
    }

    public static void teamInfo(String message) {
	for (Player player : Bukkit.getServer().getOnlinePlayers()) {
	    if (Utils.hasPermission(player)) {
		player.sendMessage(message);
	    }
	}
    }

    public static boolean hasPermission(Player player) {
	return player.hasPermission(Main.getMessageManager().getMessage("Permission"));
    }

    public static void autoAssign(Player p) {
	List<Ticket> tickets = Main.getTicketManager().getOpenTickets();
	for (Ticket ticket : tickets) {
	    if (ticket.getSupporter() == null) {
		Utils.teleportToTicket(p, ticket);
		Utils.assignTicket(p, ticket);
		return;
	    }
	}
	for (Ticket ticket : tickets) {
	    Utils.teleportToTicket(p, ticket);
	    Utils.assignTicket(p, ticket);
	    return;
	}
    }

    public static void unassignTicket(Player p, Ticket ticket) {
	OfflinePlayer pt = Bukkit.getServer().getOfflinePlayer(ticket.getCreator());
	String message = Main.getMessageManager().getMessage("Ticket_Unassigned", "id", ticket.getId(), "supporter",
		ticket.getSupporterName());
	ticket.setSupporter(null);
	if (pt.isOnline()) {
	    ((CommandSender) pt).sendMessage(message);
	}
	Utils.teamInfo(message);
    }

    public static void closeTicket(Player p, Ticket ticket, String[] a) {
	String reason = null;
	if (a != null) {
	    reason = Utils.getArgs(a, 2);
	}
	MessageManager messageManager = Main.getMessageManager();
	String message = messageManager.getMessage("Ticket_Closed", "id", ticket.getId(), "answer", reason, "supporter",
		p.getName());
	OfflinePlayer pt = Bukkit.getOfflinePlayer(ticket.getCreator());
	if (pt.isOnline()) {
	    ((CommandSender) pt).sendMessage(message);
	    ticket.delete();
	} else {
	    ticket.setAnswer(reason);
	    ticket.setOpen(false);
	    ticket.setSupporter(p.getUniqueId());
	}
	Utils.teamInfo(message);
	// for (int i : Main.TicketList.keySet()) {
	// if (i > closedTicketId) {
	// int Tickets = 0;
	// for (int ii : Main.TicketList.keySet()) {
	// if (ii < i) {
	// Tickets = Tickets + 1;
	// }
	// }
	// String nachricht = "";
	// if (Tickets == 0) {
	// nachricht = "Es liegt kein Ticket mehr vor deinem Ticket.";
	// } else if (Tickets == 1) {
	// nachricht = "Es liegt noch " + Tickets + " Ticket vor deinem Ticket.";
	// } else if (Tickets >= 2) {
	// nachricht = "Es liegen noch " + Tickets + " Tickets vor deinem Ticket.";
	// }
	//
	// OfflinePlayer ptt =
	// Main.plugin.getServer().getOfflinePlayer(Main.TicketList.get(i));
	// if (ptt != null && ptt.isOnline()) {
	// ((CommandSender) ptt).sendMessage(Main.pPrefix + "Ein Ticket vor deinem
	// Ticket Nummer " + i
	// + " wurde bearbeitet. " + nachricht);
	// }
	// }
	// }
    }

    public static void sendToServer(Player player, String serverName) {
	System.out.println("Sending " + player.getName() + " to server " + serverName + "...");
	final ByteArrayOutputStream b = new ByteArrayOutputStream();
	final DataOutputStream out = new DataOutputStream(b);
	try {
	    out.writeUTF("Connect");
	    out.writeUTF(serverName);
	} catch (final IOException ex) {
	    ex.printStackTrace();
	}
	player.sendPluginMessage(Main.getInstance(), "BungeeCord", b.toByteArray());
    }

    public static String getArgs(final String[] args, final int num) {
	final StringBuilder sb = new StringBuilder();
	for (int i = num; i < args.length; ++i) {
	    sb.append(args[i]).append(" ");
	}
	return sb.toString().trim();
    }
}
