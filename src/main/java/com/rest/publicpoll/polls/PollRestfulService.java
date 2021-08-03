package com.rest.publicpoll.polls;

import java.util.ArrayList;

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

import com.rest.publicpoll.Poll;
import com.rest.publicpoll.PollAnswer;
import com.rest.publicpoll.PollComment;

/*
 * Poll Requests RESTful Service
 *
 * Rest KEY:
 * 
 * getPoll/id:
 * 	GET
 * 	PathParam: id(String)
 * 	Produces: JSON
 * 
 * addPoll:
 * 	POST
 * 	Consumes: JSON
 * 	Produces: Plain Text
 * 
 * addSelection:
 * 	POST
 * 	Consumes: JSON
 * 	Produces: JSON
 * 
 * addComment:
 * 	POST
 * 	Consumes: JSON
 * 	Produces: JSON
 * 
 */

/*
 * TODO:
 * Finish adding Rest API service to the rest of the methods in AdjustPollDatabase
 * Missing includes delete
 */

@Path("/poll")
public class PollRestfulService 
{
	@Path("/getPoll/{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPollByID(@PathParam("id") String id) {
		AdjustPollDatabase database = new AdjustPollDatabase();
		String jsonResponse = database.returnPollJSONFromID(id);
		//Checks if response is JSON or error
		if (jsonResponse.charAt(0) == '{') {
			return Response.status(200).entity(jsonResponse).build();
		}
		else
			return Response.status(406).build();
			
	}
	
	@Path("/addPoll")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public Response createPoll(String pollJSON) {
		AdjustPollDatabase database = new AdjustPollDatabase();
		Poll poll = Poll.fromJSONCreation(pollJSON);
		String jsonResponse = database.addNewPoll(poll);
		return Response.status(201).entity(jsonResponse).build();
	}
	
	@Path("/addComment")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addComment(String pollCommentJSON) {
		AdjustPollDatabase database = new AdjustPollDatabase();
		PollComment comment = PollComment.fromJSON(pollCommentJSON);
		String jsonResponse = database.addComment(comment);
		return Response.status(201).entity(jsonResponse).build();
	}
	
	@Path("/deleteComment/{id}")
	@DELETE
	@Produces(MediaType.TEXT_PLAIN)
	public Response deleteComment(@PathParam("id") String id) {
		AdjustPollDatabase database = new AdjustPollDatabase();
		return Response.status(202).entity(database.deleteComment(id)).build();
	}
	
	@Path("/deletePoll/{id}")
	@DELETE
	@Produces(MediaType.TEXT_PLAIN)
	public Response deletePoll(@PathParam("id") String id) {
		AdjustPollDatabase database = new AdjustPollDatabase();
		return Response.status(202).entity(database.deletePoll(id)).build();
	}
	
	@Path("/getPolls/{userID}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPolls(@PathParam("userID") String userID) {
		AdjustPollDatabase database = new AdjustPollDatabase();
		String jsonResponse = database.getRandomListOfPolls(userID);
		return Response.status(200).entity(jsonResponse).build();
	}
	
	
	@Path("/getPollsFromPollIDs")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPollsFromPollIDs(String pollIDsJSON) { 
		AdjustPollDatabase database = new AdjustPollDatabase();
		ArrayList<String> pollIDs = new ArrayList<String>();
		JSONObject jo = new JSONObject(pollIDsJSON);
		JSONArray ja = (JSONArray) jo.get("pollIDs");
		for (int i = 0; i < ja.length(); i++) {
			pollIDs.add(ja.getString(i));
		}
		String jsonResponse = database.getPollsJSONFromPollIDs(pollIDs);
		return Response.status(201).entity(jsonResponse).build();
		
	}
	
	@Path("/addUserResponseToPoll")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addUserResponseToPoll(String json) {
		JSONObject jo = new JSONObject(json);
		String pollID = jo.getString("pollID");
		String userID = jo.getString("userID");
		String letter = jo .getString("letter");
		
		AdjustPollDatabase database = new AdjustPollDatabase();
		return Response.status(201).entity(database.addUserResponseToPoll(pollID, userID, letter)).build();
		
	}
	
	
}