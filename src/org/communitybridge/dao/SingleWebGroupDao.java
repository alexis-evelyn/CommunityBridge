package org.communitybridge.dao;

import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.communitybridge.dao.WebGroupDao.EMPTY_LIST;
import org.communitybridge.main.Configuration;
import org.communitybridge.main.SQL;
import org.communitybridge.utility.Log;
import org.communitybridge.utility.StringUtilities;

public class SingleWebGroupDao extends WebGroupDao
{
	public static final String EXCEPTION_MESSAGE_GETSECONDARY = "Exception during SingleMethodWebGroupDao.getSecondaryGroups(): ";
	public static final String EXCEPTION_MESSAGE_GETPRIMARY_USERIDS = "Exception during SingleMethodWebGroupDao.getPrimaryGroupUserIDs(): ";
	public static final String EXCEPTION_MESSAGE_GETSECONDARY_USERIDS = "Exception during SingleMethodWebGroupDao.getSecondaryGroupUserIDs(): ";
	public SingleWebGroupDao(Configuration configuration, SQL sql, Log log)
	{
		super(configuration, sql, log);
	}

	@Override
	public void addGroup(String userID, String groupID, int currentGroupCount) throws IllegalAccessException, InstantiationException, MalformedURLException, SQLException
	{
		if (currentGroupCount > 1)
		{
			groupID = configuration.webappSecondaryGroupGroupIDDelimiter + groupID;
		}
		String query = "UPDATE `" + configuration.webappSecondaryGroupTable + "` "
								 + "SET `" + configuration.webappSecondaryGroupGroupIDColumn + "` = CONCAT(`" + configuration.webappSecondaryGroupGroupIDColumn + "`, '" + groupID + "') "
								 + "WHERE `" + configuration.webappSecondaryGroupUserIDColumn + "` = '" + userID + "'";
		sql.updateQuery(query);
	}

	@Override
	public void removeGroup(String userID, String groupID) throws IllegalAccessException, InstantiationException, MalformedURLException, SQLException
	{
		String query = "SELECT `" + configuration.webappSecondaryGroupGroupIDColumn + "` "
								 + "FROM `" + configuration.webappSecondaryGroupTable + "` "
								 + "WHERE `" + configuration.webappSecondaryGroupUserIDColumn + "` = '" + userID + "'";
		result = sql.sqlQuery(query);
		
		if (result.next())
		{
			String groupIDs = result.getString(configuration.webappSecondaryGroupGroupIDColumn);
			List<String> groupIDsAsList = new ArrayList<String>(Arrays.asList(groupIDs.split(configuration.webappSecondaryGroupGroupIDDelimiter)));
			groupIDsAsList.remove(groupID);
			groupIDs = StringUtilities.joinStrings(groupIDsAsList, configuration.webappSecondaryGroupGroupIDDelimiter);
			query = "UPDATE `" + configuration.webappSecondaryGroupTable + "` "
						+ "SET `" + configuration.webappSecondaryGroupGroupIDColumn + "` = '" + groupIDs + "' "
						+ "WHERE `" + configuration.webappSecondaryGroupUserIDColumn + "` = '" + userID + "'";
			sql.updateQuery(query);
		}		
	}
	
	@Override
	public List<String> getUserSecondaryGroupIDs(String userID) throws IllegalAccessException, InstantiationException,MalformedURLException, SQLException
	{
		if (!configuration.webappSecondaryGroupEnabled)
		{
			return EMPTY_LIST;
		}
		String query =
						"SELECT `" + configuration.webappSecondaryGroupGroupIDColumn + "` "
					+ "FROM `" + configuration.webappSecondaryGroupTable + "` "
					+ "WHERE `" + configuration.webappSecondaryGroupUserIDColumn + "` = '" + userID + "' ";

			result = sql.sqlQuery(query);

			if (result.next())
			{
				return convertDelimitedIDString(result.getString(configuration.webappSecondaryGroupGroupIDColumn));
			}
			return EMPTY_LIST;
	}

	@Override
	public List<String> getGroupUserIDs(String groupID)
	{
		List<String> userIDs = getGroupUserIDsPrimary(groupID);
		userIDs.addAll(getGroupUserIDsSecondary(groupID));
		
		return userIDs;
	}

	@Override
	public List<String> getGroupUserIDsPrimary(String groupID)
	{
		List<String> userIDs = new ArrayList<String>();
		
		if (!configuration.webappPrimaryGroupEnabled)
		{
			return userIDs;
		}
		
		String query =
						"SELECT `" + configuration.webappPrimaryGroupUserIDColumn + "` "
						+ "FROM `" + configuration.webappPrimaryGroupTable + "` "
						+ "WHERE `" + configuration.webappPrimaryGroupGroupIDColumn + "` = '" + groupID + "' ";
		try
		{
			result = sql.sqlQuery(query);
			while(result.next())
			{
				userIDs.add(result.getString(configuration.webappPrimaryGroupUserIDColumn));
			}
			return userIDs;
		}
		catch (SQLException exception)
		{
			log.severe(EXCEPTION_MESSAGE_GETPRIMARY_USERIDS + exception.getMessage());
			return userIDs;
		}
		catch (MalformedURLException exception)
		{
			log.severe(EXCEPTION_MESSAGE_GETPRIMARY_USERIDS + exception.getMessage());
			return userIDs;
		}
		catch (InstantiationException exception)
		{
			log.severe(EXCEPTION_MESSAGE_GETPRIMARY_USERIDS + exception.getMessage());
			return userIDs;
		}
		catch (IllegalAccessException exception)
		{
			log.severe(EXCEPTION_MESSAGE_GETPRIMARY_USERIDS + exception.getMessage());
			return userIDs;
		}
	}

	@Override
	public List<String> getGroupUserIDsSecondary(String groupID)
	{
		List<String> userIDs = new ArrayList<String>();
		
		if (!configuration.webappSecondaryGroupEnabled)
		{
			return userIDs;
		}
		
		String query =
						"SELECT `" + configuration.webappSecondaryGroupUserIDColumn + "`, `" + configuration.webappSecondaryGroupGroupIDColumn + "` "
						+ "FROM `" + configuration.webappSecondaryGroupTable + "` ";
		try
		{
			result = sql.sqlQuery(query);
			while(result.next())
			{
				String groupIDs = result.getString(configuration.webappSecondaryGroupGroupIDColumn);
				if (groupIDs != null)
				{
					groupIDs = groupIDs.trim();
					if (!groupIDs.isEmpty())
					{
						for (String id : groupIDs.split(configuration.webappSecondaryGroupGroupIDDelimiter))
						{
							if (id.equals(groupID))
							{
								userIDs.add(result.getString(configuration.webappSecondaryGroupUserIDColumn));
							}
						}
					}
				}
			}
			return userIDs;
		}
		catch (SQLException exception)
		{
			log.severe(EXCEPTION_MESSAGE_GETSECONDARY_USERIDS + exception.getMessage());
			return userIDs;
		}
		catch (MalformedURLException exception)
		{
			log.severe(EXCEPTION_MESSAGE_GETSECONDARY_USERIDS + exception.getMessage());
			return userIDs;
		}
		catch (InstantiationException exception)
		{
			log.severe(EXCEPTION_MESSAGE_GETSECONDARY_USERIDS + exception.getMessage());
			return userIDs;
		}
		catch (IllegalAccessException exception)
		{
			log.severe(EXCEPTION_MESSAGE_GETSECONDARY_USERIDS + exception.getMessage());
			return userIDs;
		}
	}
}