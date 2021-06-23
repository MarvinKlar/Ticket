package mr.minecraft15.ticket.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import mr.minecraft15.ticket.Main;

public class Ticket {
	private int id;
	private boolean open;
	private UUID creator;
	private UUID supporter;
	private String server;
	private Location location;
	private String request;
	private String answer;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;

		TicketManager ticketManager = Main.getTicketManager();
		try {
			Connection connection = ticketManager.getConnection();
			Statement statement = connection.createStatement();
			statement.executeUpdate(
					"UPDATE Tickets SET open = " + (open ? "TRUE" : "FALSE") + " WHERE id = " + getId() + ";");
			statement.close();
		} catch (SQLException e) {
			System.out.println("Unable to update ticket state (open=" + open + "):");
			e.printStackTrace();
		}

		ticketManager.onChange();
	}

	public UUID getCreator() {
		return creator;
	}

	public String getCreatorName() {
		return Bukkit.getOfflinePlayer(getCreator()).getName();
	}

	public void setCreator(UUID creator) {
		this.creator = creator;
	}

	public UUID getSupporter() {
		return supporter;
	}

	public String getSupporterName() {
		return getSupporter() == null ? Main.getMessageManager().getMessage("No_Supporter")
				: Bukkit.getOfflinePlayer(getSupporter()).getName();
	}

	public void setSupporter(UUID supporter) {
		this.supporter = supporter;

		TicketManager ticketManager = Main.getTicketManager();
		try {
			Connection connection = ticketManager.getConnection();
			Statement statement = connection.createStatement();
			if (supporter == null) {
				statement.executeUpdate("UPDATE Tickets SET supporter = NULL WHERE id = " + getId() + ";");
			} else {
				statement.executeUpdate(
						"UPDATE Tickets SET supporter = '" + supporter.toString() + "' WHERE id = " + getId() + ";");
			}
			statement.close();
		} catch (SQLException e) {
			System.out.println("Unable to update ticket supporter (supporter=" + supporter + "):");
			e.printStackTrace();
		}

		ticketManager.onChange();
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public String getRequest() {
		return request;
	}

	public void setRequest(String request) {
		this.request = request;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;

		TicketManager ticketManager = Main.getTicketManager();
		if (answer != null) {
			try {
				Connection connection = ticketManager.getConnection();
				PreparedStatement statement = connection
						.prepareStatement("UPDATE Tickets SET answer = ? WHERE id = " + getId() + ";");
				statement.setString(1, answer);
				statement.execute();
				statement.close();
			} catch (SQLException e) {
				System.out.println("Unable to update ticket answer (answer=" + answer + "):");
				e.printStackTrace();
			}

			ticketManager.onChange();
		}
	}

	public Ticket(Player player, String message) {
		TicketManager ticketManager = Main.getTicketManager();
		MessageManager messageManager = Main.getMessageManager();

		int tickets = ticketManager.getTickets(player).size();
		if (tickets >= Main.getMaxTickets()) {
			System.out.println("The player " + player.getName() + " has already created " + tickets
					+ " tickets from the maximum of " + Main.getMaxTickets() + " tickets.");
			player.sendMessage(messageManager.getMessage("Max_Tickets_Exceeded"));
			return;
		}

		open = true;
		creator = player.getUniqueId();
		server = Main.getInstance().getServerName();
		location = player.getLocation();
		request = message;

		try {
			Connection connection = ticketManager.getConnection();

			PreparedStatement preparedStatement = connection.prepareStatement(
					"INSERT INTO Tickets (open, creator, server, world, x, y, z, request) VALUES (TRUE, '"
							+ creator.toString() + "', '" + server + "', '" + location.getWorld().getName() + "', '"
							+ location.getX() + "', '" + location.getY() + "', '" + location.getZ() + "', ?);");
			preparedStatement.setString(1, request);
			preparedStatement.execute();
			preparedStatement.close();

			Statement statement = connection.createStatement();
			ResultSet resultset = statement.executeQuery("SELECT LAST_INSERT_ID();");
			resultset.next();
			this.id = resultset.getInt(1);
			statement.close();
			resultset.close();

		} catch (SQLException e) {
			player.sendMessage(messageManager.getMessage("Unable_To_Create_Ticket"));
			System.out.println("Unable to create write ticket to database:");
			e.printStackTrace();
			return;
		}
		ticketManager.getTicketCache().add(this);
		ticketManager.onChange();

		tickets++;

		player.sendMessage(messageManager.getMessage("Ticket_Created", "id", getId()));

		Utils.teamInfo(messageManager.getMessage("Ticket_Created_Info", "id", getId(), "creator", getCreatorName(),
				"tickets", tickets, "max_tickets", Main.getMaxTickets(), "request", getRequest()));

		for (Player p : Main.getInstance().getServer().getOnlinePlayers()) {
			if (Utils.hasPermission(p)) {
				Utils.listTicketMessage(p, this, "List_Ticket_With_Assign_Option");
			}
		}
	}

	public Ticket(int id, boolean open, UUID creator, UUID supporter, String server, Location location, String request,
			String answer) {
		this.id = id;
		this.open = open;
		this.creator = creator;
		this.supporter = supporter;
		this.server = server;
		this.location = location;
		this.request = request;
		this.answer = answer;
	}

	public void delete() {
		System.out.println("Deleting ticket #" + this.getId() + "...");

		TicketManager ticketManager = Main.getTicketManager();

		try {
			Connection connection = ticketManager.getConnection();
			Statement statement = connection.createStatement();
			statement.executeUpdate("DELETE FROM Tickets WHERE id = " + getId() + ";");
			statement.close();

		} catch (SQLException e) {
			System.out.println("Unable to delete ticket from database:");
			e.printStackTrace();
			return;
		}

		ticketManager.getTicketCache().remove(this);
		ticketManager.onChange();
	}
}
