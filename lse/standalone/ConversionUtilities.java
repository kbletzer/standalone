/* author: K. Bletzer */
/* Last updated August 31, 2011 */
package lse.standalone;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ConversionUtilities 
{

	/* take a string and tokenize using string manipulation and 
	 * knowledge about string format: 		
	 * either the first token in the string is a quoted string
	 * or it is a value offset by spaces.
	 *
	 * @param s - string to tokenize
	 * @return - a list of tokens extracted from input string s
	 */
	public ArrayList<String> extractTokens(String s)
	{

		ArrayList<String> tokenList = new ArrayList<String>();
		String token; 
		s = s.trim();
		
		int beginTkn, endTkn;
		beginTkn = 0;
		boolean stringToken = false;
		
		while (s.length() > 0)
		{
			stringToken = false;
			if (s.charAt(beginTkn) == '\"')
			{
				endTkn = s.indexOf("\"", beginTkn + 1) + 1;
				stringToken = true;
			}
			else
			{
				endTkn = s.indexOf(" ", beginTkn + 1);
			}
			
			if (endTkn < 0) { endTkn = s.length();  }
			
			token = this.removeQuoteMarks(s.substring(beginTkn, endTkn));
			
			if (stringToken || token.trim().length() > 0)
			{
				tokenList.add(token);  //add any token if enclosed in "" marks, even if blank
			}

			s = s.substring(endTkn).trim();
		}
		
		return tokenList;
	}
	
	/* remove the beginning and end quotation marks from a string
	 * if they are present.
	 */
	public String removeQuoteMarks(String q)
	{
		Pattern pat = Pattern.compile("\"");
		Matcher m = pat.matcher(q);

        return m.replaceAll("");
	}
	
	/* Convenience method for pretty printing the contents of a string array */
	public void printStringArray(String[] s)
	{
		for(int i =0; i < s.length; i++)
		{
			System.out.println(i+" - |" + s[i] +"| ");
		}
	} 
	
	/* Convenience method for formatting an ArrayList for output */
	public String arrayListToBracketList(ArrayList<String> al)
	{
		String s = al.toString();
		s = s.replace("]", "");
		s = s.replace("[", "");
		s = s.replace(",", "");
		return "{ " + s + " }";
	}
	
	public void createFile(String name, String contents)
	{
		try 
		{
		    BufferedWriter out = new BufferedWriter(new FileWriter(name));
		    out.write(contents);
		    out.close();
		} 
		catch (IOException e) 
		{
			System.out.println("ConversionUtilities Exception: " +e.toString());
		}
	}
	
	/* Extract an attribute from an Element/Node */
	/* Based on similar function in gte ExtensiveFormXMLReader */
	public String getAttribute(Node elem, String key)
	{
		String value = null;
		if (elem != null && elem.getAttributes().getNamedItem(key) != null) 
		{
			value = elem.getAttributes().getNamedItem(key).getNodeValue();
		}
		return value;
	}
	
	/* read an xml file and create corresponding XML document */
	public Document fileToXML(String filename)
	{
		Document doc = null;
		try 
		{
			File file = new File(filename);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

			DocumentBuilder db;
			db = dbf.newDocumentBuilder();
			doc = db.parse(file);
		} 
		catch (Exception e) 
		{
			System.out.println("fileToXML: " + e.toString());
		}
		return doc;
		
	}
	
	/* read the <players> XML element and create an ArrayList representing
	 * the ordered set of players.
	 */
	public ArrayList<String> readPlayersXML(Node players)
	{
		ArrayList<String> orderedPlayers = new ArrayList<String>();
		for (Node child = players.getFirstChild(); child != null; child =  child.getNextSibling()) 
		{
			if ("player".equals(child.getNodeName())) 
			{
				String playerId = getAttribute(child, "playerId");
				int id = Integer.parseInt(playerId);
				if (id < orderedPlayers.size()) 
				{
					orderedPlayers.add(id-1, child.getTextContent());
				}
				else
				{
					orderedPlayers.add(child.getTextContent());
				}
			} 
		} 
		return orderedPlayers;
	}
	
	/* create the <players> XML node in the Document xmlDoc
	 * based on a list of players provided to the function.
	 */
	public Element createPlayersNode(ArrayList<String> players, Document xmlDoc)
	{
		Element elPlayers = xmlDoc.createElement("players");

		for (int i = 1; i <= players.size(); i++)
		{
		   String playerName = players.get(i-1).trim();
		   Element child = xmlDoc.createElement("player");
		   child.setAttribute("playerId", ""+i);
		   
		   if (playerName.length() !=0) {  child.setTextContent(playerName); }
		   else { child.setTextContent(""+i); }
		   
		   elPlayers.appendChild(child);
		}
		
		return elPlayers;
	}
	
	/* update an already existing <players> XML node in the Document xmlDoc
	 * with the list of players provided to the function.
	 */
	public Node updatePlayersNode(ArrayList<String> players, Document xmlDoc, Node elPlayers)
	{
		for (int i = 1; i <= players.size(); i++)
		{
		   String playerName = players.get(i-1).trim();
		   Element child = xmlDoc.createElement("player");
		   child.setAttribute("playerId", ""+i);
		   
		   if (playerName.length() !=0) {  child.setTextContent(playerName); }
		   else { child.setTextContent(""+i); }
		   
		   elPlayers.appendChild(child);
		}
		
		return elPlayers;
	}

	/* for all outcome or node tags sort the attributes in the tags
	 * according to a predetermined order.
	 */
	public String orderXMLAttributes(String origXML)
	{
		String newXML = "";
		//match the start tag of either outcome or node elements
		//Pattern p = Pattern.compile("<(node|outcome).*>");
		Pattern p = Pattern.compile("<(node|outcome).*\\/?>");

		Matcher m = p.matcher(origXML);

		while (m.find()) 
		{
			String s = m.group();

			//strip off characters that are not part of the attribute list
			s = s.replaceAll("<(node|outcome)", "");
			s = s.replace("/>", "").trim();
			s = s.replace(">", "").trim();
			
			List<String> attributesToArrayList = (List<String>)this.attributesToArrayList(s);
			List<String> list = this.sortAttributes(attributesToArrayList);
			String s_new = "";
			
			for (int i = 0; i < list.size(); i++)
			{
				s_new += list.get(i) + " ";
			}
			
			s = Pattern.quote(s);
			newXML = origXML.replaceFirst(s, s_new.trim());
		}
				
		return newXML; 
	}
	
	/* Sort the attributes in the attributes List and return a List.
	 */
	private List<String> sortAttributes(List<String> al)
	{
		List<String> orderList = this.getAttributeOrderMapping();
		ArrayList<String> attributes 
			= new ArrayList<String>(Arrays.asList("", "", "", "", "", "", "", "", "", ""));
		
		if (al.size() > 1)
		{
			for(int j = 0; j < al.size(); j++)
			{
				String currAttr = al.get(j);
				String key = currAttr.substring(0, currAttr.indexOf("="));
				attributes.set(orderList.indexOf(key), currAttr);
			}
			
			//cleanup blanks leaving only attributes
			attributes.removeAll(Arrays.asList(""));  
		}
		
		return al;
	}
	
	/* The order for attributes is defined here.
	 */
	private List<String> getAttributeOrderMapping()
	{
		List<String> al = new ArrayList<String>();
		
		al.add("iset");
		al.add("isetName");
		al.add("player");
		al.add("move");
		al.add("prob");
		al.add("nodeName");
		al.add("outcomeId");
		al.add("outcomeName");

		return al;
	}
	
	/* Take a string containing attributes and transform to a List.
	 */
	private List<String> attributesToArrayList(String attr)
	{
		//match attribute of form attrName="value"
		Pattern p = Pattern.compile("[a-zA-Z]+=\"[^\"]*\"");

		Matcher m = p.matcher(attr);
		ArrayList<String> al = new ArrayList<String>();

		while (m.find()) 
		{
			String s = m.group();
			al.add(s);
		}
		
		return al;
	}
}
