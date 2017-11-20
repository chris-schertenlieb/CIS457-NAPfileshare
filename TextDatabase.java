import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.*;
import java.io.*;

/**
 * A custom static class for working with tables unique to this application
 * Files users.txt and files.txt must be in the same directory as this class.
 */
public class TextDatabase {

    private static String userDB = "users.txt";
    private static String fileDB = "files.txt";

    /*
     * Main method for a sanity check.
     * Prints all usernames in the database to the console
     */
    public static void main(String[] args)
    {
        ArrayList<String> result = getAllUsernames();

        for (String username : result)
        {
            System.out.println(username);
        }
    }

    /*
     * Returns an ArrayList<String> of all usernames in the database.
     * Good for a sanity check.
     */
    public synchronized static ArrayList<String> getAllUsernames()
    {
        ArrayList<String> allRows = getFileContents(userDB);
        ArrayList<String> result = new ArrayList<String>();

        for (String row : allRows)
        {
            result.add(row.split(",")[0]);
        }

        return result;
    }

    /*
     * Returns true if the username already exists in the database
     */
    public synchronized static Boolean checkForExistingUser(String username)
    {
        ArrayList<String> allUsers = getFileContents(userDB);

        for (String row : allUsers)
        {
            if (row.split(",")[0].equals(username))
                return true;
        }

        return false;

    }

    /*
     * Checks to see that the username does not exists already
     * and then inserts it.
     *
     * Throws Exception with message "Username already exists"
     */
    public synchronized static void insertRowIntoUsers(String username, String hostname, String connectionSpeed) throws Exception
    {
        if (checkForExistingUser(username))
            throw new Exception("Username already exists");

        /* append new data to end of file */
        try(PrintWriter out = new PrintWriter(
                new BufferedWriter(
                        new FileWriter(userDB, true))))
        {
            out.println(username + "," + hostname + "," + connectionSpeed);
        }
        catch(Exception e){
        }
    }

    /*
     * Checks to see that the username exists in the users table (it should)
     * and then inserts the row into files database.
     *
     * Quietly returns w/o updating if duplicate row already exists
     *
     * Throws Exception with message "Username does not exist"
     */
    public synchronized static void insertRowIntoFiles(String username, String path, String address) throws Exception
    {
        if (!checkForExistingUser(username))
            throw new Exception("Username does not exist");

        ArrayList<String> result = getFileContents(fileDB);

        Boolean duplicateExists = false;
        for (String row: result)
        {
            if (row.equals(username + "," + path))
            {
                duplicateExists = true;
                break;
            }
        }

        if (!duplicateExists)
        {
            try(PrintWriter out = new PrintWriter(
                    new BufferedWriter(
                            new FileWriter(fileDB, true))))
            {
                out.println(username + "," + path + "," + address);
            }
        }
    }


    /*
     * Removes all files relevant to a single username.
     *
     *  Quietly fails if that username is not in the users table.
     */
    public synchronized static void deleteSingleUsersFilesAndKeywords(String username)
    {
        if (!checkForExistingUser(username))
            return;

        ArrayList<String> result = getFileContents(fileDB);

        for (int i=0; i<result.size(); i++)
        {
            if (result.get(i).split(",")[0].equals(username))
            {
                result.remove(i);
                i--;
            }
        }

        updateTable(fileDB, result);
    }

    /*
     * Removes the username from the users table
     *  in addition to all related files
     */
    public synchronized static void deleteUserCascade(String username)
    {
        if (!checkForExistingUser(username))
            return;

        ArrayList<String> result = getFileContents(userDB);

        for (int i=0; i<result.size(); i++)
        {
            if (result.get(i).split(",")[0].equals(username))
            {
                result.remove(i);
                break;
            }
        }

        updateTable(userDB, result);

        result = getFileContents(fileDB);
        for (int i=0; i<result.size(); i++)
        {
            if (result.get(i).split(",")[0].equals(username))
            {
                result.remove(i);
                i--;
            }
        }

        updateTable(fileDB, result);
    }

    /*
     * Removes a file from the files table
     */
    public synchronized static void deleteFileCascade(String username, String path)
    {
        ArrayList<String> result = getFileContents(fileDB);
        for (int i=0; i<result.size(); i++)
        {
            if (result.get(i).equals(username + "," + path))
            {
                result.remove(i);
                break;
            }
        }

        updateTable(fileDB, result);
    }

    /*
     * Returns an ArrayList<String> of "username,path"s
     *  for files with requisite keywords
     */
    public synchronized static ArrayList<String> searchByKeyword(String keyword)
    {
        ArrayList<String> allFiles = getFileContents(fileDB);
        ArrayList<String> result = new ArrayList<String>();

        for (int i=0; i<allFiles.size(); i++)
        {
            if (allFiles.get(i).contains(keyword))
            {
                result.add(allFiles.get(i));
            }
        }

        return result;
    }

    /*
     * Replaces the data in a table completely with new data
     */
    public synchronized static void updateTable(String filename, ArrayList<String> data)
    {
        try(PrintWriter out = new PrintWriter(
                new BufferedWriter(
                        new FileWriter(filename))))
        {
            for (String row : data)
            {
                out.println(row);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * Helper method that loads the database from a file into an ArrayList<String>
     */
    public synchronized static ArrayList<String> getFileContents(String fileName)
    {
        ArrayList<String> result = new ArrayList<String>();

        String line = null;

        try {
            BufferedReader bufferedReader = new BufferedReader(
                    new FileReader(fileName));

            while((line = bufferedReader.readLine()) != null) {
                result.add(line);
            }


            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                "Unable to open file '" +
                fileName + "'");
            //ex.printStackTrace();
        }
        catch(IOException ex) {
            System.out.println(
                "Error reading file '"
                + fileName + "'");
            // ex.printStackTrace();
        }

        return result;
    }

    public synchronized static String getUserServer(String targetUser){
      ArrayList<String> users = getFileContents(userDB);
      String[] tempUsers = new String[3];
      String temp = "";
      for(int i=0; i<users.size(); i++){
        temp = users.get(i);
        tempUsers = temp.split(",");
        if(tempUsers[0].equals(targetUser)){
          return tempUsers[1];
        }
      }
      return "";
    }
}
