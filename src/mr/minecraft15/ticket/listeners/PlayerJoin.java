package mr.minecraft15.ticket.listeners;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import mr.minecraft15.ticket.Main;
import mr.minecraft15.ticket.utils.MessageManager;
import mr.minecraft15.ticket.utils.Ticket;
import mr.minecraft15.ticket.utils.Utils;

public class PlayerJoin implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
	Player p = e.getPlayer();
	MessageManager messageManager = Main.getMessageManager();
	List<Ticket> unreadTickets = Main.getTicketManager().getUnreadTickets(p);

	if (p.hasPermission(messageManager.getMessage("Permission"))) {
	    Utils.sendOpenTickets(p);
	}

	if (!unreadTickets.isEmpty()) {
	    for (Ticket ticket : unreadTickets) {
		p.sendMessage(messageManager.getMessage("Ticket_Closed", "id", ticket.getId(), "answer",
			ticket.getAnswer(), "supporter", ticket.getSupporterName()));
		ticket.delete();
	    }
	}
    }
}
