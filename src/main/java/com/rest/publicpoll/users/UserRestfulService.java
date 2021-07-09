package com.rest.publicpoll.users;

import java.util.ArrayList;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import com.rest.publicpoll.User;

@Path("/users")
public class UserRestfulService {

	@Path("/signInUsername")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@POST
	public Response signInUsername(String JSON) {
		JSONObject jo = new JSONObject(JSON);
		String username = jo.getString("username");
		String password = jo.getString("password");
		String jsonResponse = AdjustUsersDatabase.signInWithUsernameAndPassword(username, password);
		return Response.status(201).entity(jsonResponse).build();
	}
	
	
	@Path("/verify")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@POST
	//Returns email if email error, username if username error, ok if no error
	public Response verifyCreateUserInfo(String JSON) {
		JSONObject jo = new JSONObject(JSON);
		String email = jo.getString("email");
		String username = jo.getString("username");
		String jsonResponse = AdjustUsersDatabase.verifyUserData(email, username);
		return Response.status(201).entity(jsonResponse).build();
	}
	
	@Path("/createUser")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@POST
	public Response createNewUser(String JSON) {
		User user = User.newUserFromJSON(JSON);
		String jsonResponse = AdjustUsersDatabase.createNewUser(user);
		return Response.status(201).entity(jsonResponse).build();
	}
	
	@Path("/getUserByID/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@GET
	public Response getUserByID(@PathParam("id") String id) {
		String jsonResponse = AdjustUsersDatabase.getUserJSONByID(id);
		return Response.status(200).entity(jsonResponse).build();
	}
	
	@Path("/getUsers")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@POST
	public Response getUsersByIDs(String JSON) {
		JSONObject jo = new JSONObject(JSON);
		JSONArray ja = jo.getJSONArray("userIDs");
		ArrayList<String> uids = new ArrayList<String>();
		for (int i = 0; i < ja.length(); i++) {
			uids.add(ja.getString(i));
		}
		String jsonResponse = AdjustUsersDatabase.getUsersJSONByIDs(uids);
		return Response.status(201).entity(jsonResponse).build();
	}
	
	@Path("/addUserPolls")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@POST
	public Response addUserInfo(String JSON) {
		JSONObject jo = new JSONObject(JSON);
		String userID = jo.getString("userID");
		String pollID = jo.getString("pollID");
		int type = jo.getInt("type");
		return Response.status(201).entity(AdjustUsersDatabase.addUserPolls(userID, pollID, type)).build();
	}
	
}
