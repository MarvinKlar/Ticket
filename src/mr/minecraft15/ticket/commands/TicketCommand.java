package mr.minecraft15.ticket.commands;

import java.util.Optional;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import mr.minecraft15.ticket.Main;
import mr.minecraft15.ticket.utils.MessageManager;
import mr.minecraft15.ticket.utils.Ticket;
import mr.minecraft15.ticket.utils.TicketManager;
import mr.minecraft15.ticket.utils.Utils;

public class TicketCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
	Player p = (Player) s;
	MessageManager messageManager = Main.getMessageManager();
	TicketManager ticketManager = Main.getTicketManager();

	if (p instanceof Player) {
	    if (a.length == 0) {
		showMenu(p);
	    }
	    if (a.length >= 1) {
		if (a[0].equalsIgnoreCase("create") || a[0].equalsIgnoreCase("erstellen")
			|| a[0].equalsIgnoreCase("new") || a[0].equalsIgnoreCase("neu")) {
		    if (a.length == 1) {
			p.sendMessage(messageManager.getMessage("Please_Specify_Request"));
		    } else if (a.length >= 2) {
			new Ticket(p, Utils.getArgs(a, 1));
			return true;
		    }
		} else if (a[0].equalsIgnoreCase("list") || a[0].equalsIgnoreCase("liste")) {
		    if (a.length == 1) {
			if (Utils.hasPermission(p)) {
			    Utils.listTickets(p);
			} else {
			    Utils.listOwnTickets(p);
			}
		    } else if (a.length >= 2) {
			showMenu(p);
		    }
		} else if (a[0].equalsIgnoreCase("tp") || a[0].equalsIgnoreCase("teleport")
			|| a[0].equalsIgnoreCase("teleportieren") || a[0].equalsIgnoreCase("warp")) {
		    if (a.length == 2) {
			if (Utils.hasPermission(p)) {
			    Optional<Ticket> ticket = ticketManager.getOpenTicket(a[1]);
			    if (!ticket.isPresent()) {
				Utils.notExisting(p, a[1]);
				return true;
			    }
			    Utils.teleportToTicket(p, ticket.get());
			} else {
			    Utils.noPermission(p);
			}
		    } else if (a.length == 1) {
			Utils.addTicketNumber(p);
		    } else if (a.length > 2) {
			showMenu(p);
		    }
		} else if (a[0].equalsIgnoreCase("löschen") || a[0].equalsIgnoreCase("delete")
			|| a[0].equalsIgnoreCase("schließen") || a[0].equalsIgnoreCase("close")) {
		    if (Utils.hasPermission(p)) {
			if (a.length == 2) {
			    Optional<Ticket> ticket = ticketManager.getOpenTicket(a[1]);
			    if (!ticket.isPresent()) {
				Utils.notExisting(p, a[1]);
				return true;
			    }
			    Utils.closeTicket(p, ticket.get(), null);
			} else if (a.length == 1) {
			    Utils.addTicketNumber(p);
			} else if (a.length > 2) {
			    Optional<Ticket> ticket = ticketManager.getOpenTicket(a[1]);
			    if (!ticket.isPresent()) {
				Utils.notExisting(p, a[1]);
				return true;
			    }
			    Utils.closeTicket(p, ticket.get(), a);
			}
		    } else {
			if (a.length == 2) {
			    Optional<Ticket> ticket = ticketManager.getOpenTicket(a[1]);
			    if (!ticket.isPresent()) {
				Utils.notExisting(p, a[1]);
				return true;
			    }
			    if (ticket.get().getCreator() == p.getUniqueId()) {
				Utils.closeTicket(p, ticket.get(), null);
			    } else {
				p.sendMessage(messageManager.getMessage("Not_Your_Ticket", "id", a[1]));
			    }
			} else if (a.length == 1) {
			    Utils.addTicketNumber(p);
			} else if (a.length > 2) {
			    showMenu(p);
			}
		    }
		} else if (a[0].equalsIgnoreCase("assign") || a[0].equalsIgnoreCase("zuweisen")) {
		    if (Utils.hasPermission(p)) {
			if (a.length == 2) {
			    Optional<Ticket> ticket = ticketManager.getOpenTicket(a[1]);
			    if (!ticket.isPresent()) {
				Utils.notExisting(p, a[1]);
				return true;
			    }

			    Utils.assignTicket(p, ticket.get());
			} else if (a.length == 1) {
			    Utils.addTicketNumber(p);
			} else if (a.length > 2) {
			    showMenu(p);
			}
		    } else {
			Utils.noPermission(p);
		    }
		} else if (a[0].equalsIgnoreCase("unassign") || a[0].equalsIgnoreCase("abweisen")) {
		    if (Utils.hasPermission(p)) {
			if (a.length == 2) {
			    Optional<Ticket> ticket = ticketManager.getOpenTicket(a[1]);
			    if (!ticket.isPresent()) {
				Utils.notExisting(p, a[1]);
				return true;
			    }

			    if (ticket.get().getSupporter() == null) {
				p.sendMessage(messageManager.getMessage("No_Supporter_Assigned", "id", a[1]));
				return true;
			    }

			    Utils.unassignTicket(p, ticket.get());
			} else if (a.length == 1) {
			    Utils.addTicketNumber(p);
			} else if (a.length > 2) {
			    showMenu(p);
			}
		    } else {
			Utils.noPermission(p);
		    }
		} else if (a[0].equalsIgnoreCase("auto")) {
		    if (a.length >= 2) {
			Optional<Ticket> ticket = ticketManager.getOpenTicket(a[1]);
			if (!ticket.isPresent()) {
			    Utils.notExisting(p, a[1]);
			    return true;
			}

			if (ticket.get().getSupporter() == p.getUniqueId()) {
			    p.sendMessage(messageManager.getMessage("Already_Assigned", "id", ticket.get().getId()));
			    return true;
			}

			if (Utils.hasPermission(p)) {
			    Utils.teleportToTicket(p, ticket.get());
			    Utils.assignTicket(p, ticket.get());
			} else {
			    Utils.noPermission(p);
			}
		    } else if (a.length == 1) {
			Utils.addTicketNumber(p);
		    }
		} else if (a[0].equalsIgnoreCase("next")) {
		    if (Utils.hasPermission(p)) {
			if (ticketManager.getOpenTickets().isEmpty()) {
			    p.sendMessage(messageManager.getMessage("No_Open_Tickets"));
			} else {
			    Utils.autoAssign(p);
			}
		    } else {
			Utils.noPermission(p);
		    }
		} else {
		    showMenu(p);
		}
	    }
	}
	return true;
    }

    private void showMenu(Player p) {
	MessageManager messageManager = Main.getMessageManager();

	if (Utils.hasPermission(p)) {
	    for (String message : messageManager.getList("Admin_Menu")) {
		p.sendMessage(message);
	    }
	}

	for (String message : messageManager.getList("Menu")) {
	    p.sendMessage(message);
	}
    }
}
