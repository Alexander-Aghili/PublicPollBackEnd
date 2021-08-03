package com.rest.publicpoll.users;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;
import com.rest.publicpoll.BucketStorage;
import com.rest.publicpoll.User;

@Path("/users")
public class UserRestfulService {

	@Path("/signInUsername")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@POST
	public Response signInUsername(String JSON) {
		AdjustUsersDatabase usersDatabase = new AdjustUsersDatabase();
		JSONObject jo = new JSONObject(JSON);
		String username = jo.getString("username");
		String password = jo.getString("password");
		String jsonResponse = usersDatabase.signInWithUsernameAndPassword(username, password);
		return Response.status(201).entity(jsonResponse).build();
	}
	
	
	@Path("/verify")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@POST
	//Returns email if email error, username if username error, ok if no error
	public Response verifyCreateUserInfo(String JSON) {
		AdjustUsersDatabase usersDatabase = new AdjustUsersDatabase();
		JSONObject jo = new JSONObject(JSON);
		String email = jo.getString("email");
		String username = jo.getString("username");
		String jsonResponse = usersDatabase.verifyUserData(email, username);
		return Response.status(201).entity(jsonResponse).build();
	}
	
	@Path("/createUser")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@POST
	public Response createNewUser(String JSON) {
		AdjustUsersDatabase usersDatabase = new AdjustUsersDatabase();
		User user = User.newUserFromJSON(JSON);
		String jsonResponse = usersDatabase.createNewUser(user);
		return Response.status(201).entity(jsonResponse).build();
	}
	
	@Path("/getUserByID/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	public Response getUserByID(@PathParam("id") String id) {
		AdjustUsersDatabase usersDatabase = new AdjustUsersDatabase();
		String jsonResponse = usersDatabase.getUserJSONByID(id);
		return Response.status(200).entity(jsonResponse).build();
	}
	
	@Path("/getUsers")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public Response getUsersByIDs(String JSON) {
		AdjustUsersDatabase usersDatabase = new AdjustUsersDatabase();
		JSONObject jo = new JSONObject(JSON);
		JSONArray ja = jo.getJSONArray("userIDs");
		ArrayList<String> uids = new ArrayList<String>();
		for (int i = 0; i < ja.length(); i++) {
			uids.add(ja.getString(i));
		}
		String jsonResponse = usersDatabase.getUsersJSONByIDs(uids);
		return Response.status(201).entity(jsonResponse).build();
	}
	
	@Path("/addUserPolls")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@POST
	public Response addUserInfo(String JSON) {
		AdjustUsersDatabase usersDatabase = new AdjustUsersDatabase();
		JSONObject jo = new JSONObject(JSON);
		String userID = jo.getString("userID");
		String pollID = jo.getString("pollID");
		int type = jo.getInt("type");
		return Response.status(201).entity(usersDatabase.addUserPoll(userID, pollID, type)).build();
	}
	
	@Path("/checkUserPoll/{pollID}/{userID}/{type}")
	@Produces(MediaType.TEXT_PLAIN)
	@GET
	public Response checkUserPoll(@PathParam("pollID") String pollID, @PathParam("userID") String userID, @PathParam("type") String typeStr) {
		AdjustUsersDatabase usersDatabase = new AdjustUsersDatabase();
		int type = Integer.parseInt(typeStr);
		return Response.status(200).entity(usersDatabase.checkUserPollExistsResponse(userID, pollID, type)).build();
	}
	
	@Path("/deleteUserPoll/{pollID}/{userID}/{type}")
	@Produces(MediaType.TEXT_PLAIN)
	@DELETE
	public Response deleteUserPoll(@PathParam("pollID") String pollID, @PathParam("userID") String userID, @PathParam("type") String typeStr) {
		AdjustUsersDatabase usersDatabase = new AdjustUsersDatabase();
		int type = Integer.parseInt(typeStr);
		return Response.status(202).entity(usersDatabase.deleteUserPoll(userID, pollID, type)).build();
	}
	
	@Path("/uploadProfilePicture/{userID}/{replace}")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.TEXT_PLAIN)
	@POST
	public Response uploadProfilePicture(@PathParam("userID") String userID, @PathParam("replace") boolean needReplaceURL, byte[] imageData) {
		BucketStorage bucketStorage = new BucketStorage();
		AdjustUsersDatabase usersDatabase = new AdjustUsersDatabase();
		String key = userID + "-profileImage.jpg";
		String url = bucketStorage.uploadObject(key, imageData);
		
		if (needReplaceURL)
			usersDatabase.editUserData(userID, "profilePicture", url);
		
		return Response.status(201).entity(url).build();
	}
	
	@Path("/editUser")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@POST
	public Response editUser(String json) {
		AdjustUsersDatabase usersDatabase = new AdjustUsersDatabase(true);
		JSONObject jo = new JSONObject(json);
		String userID = jo.getString("userID");
		if (json.contains("firstname")) {
			usersDatabase.editUserDataMultiRequest(userID, "firstname", jo.getString("firstname"));
		}
		if (json.contains("lastname")) {
			usersDatabase.editUserDataMultiRequest(userID, "lastname", jo.getString("lastname"));
		}
		if (json.contains("username")) {
			String username = jo.getString("username");
			try {
				usersDatabase.checkExists("username", username);
				usersDatabase.editUserDataMultiRequest(userID, "username", username);
			} catch (UserDataExistsException e) {
				return Response.status(201).entity("usernameExists").build();
			} catch (SQLException e) {
				e.printStackTrace();
				return Response.status(201).entity("error").build();
			}
		}
		usersDatabase.close();
		return Response.status(201).entity("ok").build();
	}
	
	
}
