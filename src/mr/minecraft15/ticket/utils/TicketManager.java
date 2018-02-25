package mr.minecraft15.ticket.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TicketManager {

    private List<Ticket> ticketCache = new ArrayList<Ticket>();
    private Connection connection;
    private long lastConnect;

    public TicketManager(String hostname, int port, String database, String username, String password) {
	try {
	    setConnection(DriverManager.getConnection("jdbc:mysql://" + hostname + ":" + port + "/" + database
		    + "?user=" + username + "&password=" + password + "&autoreconnect=true&useSSL=false"));

	} catch (SQLException e) {
	    System.out.println("Unable to connect to the database:");
	    e.printStackTrace();
	}

	try {
	    Statement statement = getConnection().createStatement();
	    statement.executeUpdate("CREATE TABLE IF NOT EXISTS Servers (name VARCHAR(100), uptodate BOOL);");
	    statement.close();

	    statement = getConnection().createStatement();
	    statement.execute(
		    "CREATE TABLE IF NOT EXISTS Tickets (id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, open BOOL NOT NULL DEFAULT 1, creator CHAR(36) NOT NULL, supporter CHAR(36) DEFAULT NULL, server VARCHAR(100) NOT NULL, world VARCHAR(100) NOT NULL, x DOUBLE NOT NULL, y DOUBLE NOT NULL, z DOUBLE NOT NULL, request VARCHAR(100) NOT NULL, answer VARCHAR(100) DEFAULT NULL);");
	    statement.close();

	} catch (SQLException e) {
	    System.err.println("Unable to create tables:");
	    e.printStackTrace();
	}

	try {
	    Statement statement = getConnection().createStatement();
	    ResultSet resultset = statement
		    .executeQuery("SELECT * FROM Servers WHERE name='" + Bukkit.getServerName() + "';");

	    if (resultset != null && resultset.next()) {
		System.out.println("The server is already in the database.");
	    } else {
		System.out.println("The server is not in the database yet. Adding the server to database...");

		statement.close();
		statement = getConnection().createStatement();
		statement.execute("INSERT INTO Servers VALUES ('" + Bukkit.getServerName() + "', FALSE);");
		statement.close();
	    }

	    statement.close();
	    resultset.close();

	} catch (SQLException e) {
	    System.err.println("Unable to get if the server is in the database or unable to add server to database:");
	    e.printStackTrace();
	}

	loadTicketsFromDatabase();
    }

    public Optional<Ticket> getOpenTicket(Object objId) {
	try {
	    int id = Integer.parseInt(objId.toString());
	    return getTickets().stream().filter(ticket -> ticket.getId() == id && ticket.isOpen()).findFirst();
	} catch (Exception e) {
	}
	return Optional.empty();
    }

    public List<Ticket> getTickets() {
	try {
	    Statement statement = getConnection().createStatement();
	    ResultSet resultset = statement
		    .executeQuery("SELECT uptodate FROM Servers WHERE name='" + Bukkit.getServerName() + "';");

	    if (resultset != null && resultset.next()) {
		if (!resultset.getBoolean(1)) {
		    loadTicketsFromDatabase();
		}
	    }

	    statement.close();
	    resultset.close();

	} catch (SQLException e) {
	    System.err.println("Unable to get if the tickets are uptodate:");
	    e.printStackTrace();
	}

	return getTicketCache();
    }

    public void loadTicketsFromDatabase() {
	try {
	    Statement statement = getConnection().createStatement();
	    ResultSet resultset = statement.executeQuery("SELECT * FROM Tickets ORDER BY id;");

	    List<Ticket> tickets = new ArrayList<Ticket>();
	    while (resultset.next()) {
		Ticket ticket = new Ticket(resultset.getInt("id"), resultset.getBoolean("open"),
			UUID.fromString(resultset.getString("creator")),
			resultset.getString("supporter") == null ? null
				: UUID.fromString(resultset.getString("supporter")),
			resultset.getString("server"),
			new Location(Bukkit.getWorld(resultset.getString("world")), resultset.getDouble("x"),
				resultset.getDouble("y"), resultset.getDouble("z")),
			resultset.getString("request"), resultset.getString("answer"));
		tickets.add(ticket);
	    }
	    setTicketCache(tickets);

	    statement.close();
	    resultset.close();

	    statement = getConnection().createStatement();
	    statement
		    .executeUpdate("UPDATE Servers SET uptodate = TRUE WHERE name = '" + Bukkit.getServerName() + "';");
	    statement.close();
	    lastConnect = System.currentTimeMillis() / 1000;

	} catch (SQLException e) {
	    System.err.println("Unable to get the tickets from the database:");
	    e.printStackTrace();
	}
    }

    public List<Ticket> getTickets(Player p) {
	return getTickets(p.getUniqueId());
    }

    public List<Ticket> getTickets(UUID uuid) {
	return getTickets().stream().filter(ticket -> ticket.getCreator().equals(uuid)).collect(Collectors.toList());
    }

    public List<Ticket> getOpenTickets() {
	return getTickets().stream().filter(ticket -> ticket.isOpen()).collect(Collectors.toList());
    }

    public List<Ticket> getOpenTicketsWithoutSupporter() {
	return getTickets().stream().filter(ticket -> ticket.isOpen() && ticket.getSupporter() == null)
		.collect(Collectors.toList());
    }

    public List<Ticket> getUnreadTickets(Player p) {
	return getTickets().stream().filter(ticket -> !ticket.isOpen() && ticket.getCreator().equals(p.getUniqueId()))
		.collect(Collectors.toList());
    }

    public Optional<Ticket> getNextUnassignedTicket() {
	return getTickets().stream().filter(ticket -> ticket.isOpen() && ticket.getSupporter() == null).findFirst();
    }

    public Connection getConnection() {

	long currentTime = System.currentTimeMillis() / 1000;
	if (currentTime - 30 > lastConnect) {
	    System.out.println("Refreshing the connection...");

	    String connectionURL = null;
	    try {
		connectionURL = connection.getMetaData().getURL();
	    } catch (SQLException e) {
		System.err.println("Unable to get the connection string:");
		e.printStackTrace();
	    }
	    try {
		connection.close();
	    } catch (Exception e) {
	    }
	    try {
		setConnection(DriverManager.getConnection(connectionURL));
	    } catch (SQLException e) {
		System.err.println("Unable to connect to the database:");
		e.printStackTrace();
	    }
	}

	return connection;
    }

    public void setConnection(Connection connection) {
	this.connection = connection;
	lastConnect = System.currentTimeMillis() / 1000;
    }

    public List<Ticket> getTicketCache() {
	return ticketCache;
    }

    public void setTicketCache(List<Ticket> ticketCache) {
	this.ticketCache = ticketCache;
    }

    public void onChange() {
	try {
	    Statement statement = getConnection().createStatement();

	    statement.executeUpdate(
		    "UPDATE Servers SET uptodate = FALSE WHERE name != '" + Bukkit.getServerName() + "';");

	    statement.close();

	    lastConnect = System.currentTimeMillis() / 1000;
	} catch (SQLException e) {
	    System.err.println("Unable to set other servers to uptodate=false:");
	    e.printStackTrace();
	}
    }

}
