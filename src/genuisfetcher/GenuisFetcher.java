/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package genuisfetcher;

import java.io.IOException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author Fichée_SS
 */
public class GenuisFetcher {

    /**
     * @param args the command line arguments
     */
    @SuppressWarnings("null")
    public static void main(String[] args) {

        Options options = new Options();

        options.addOption(OptionBuilder
                .withArgName("Artits Name")
                .hasArg()
                .isRequired(true)
                .withDescription("input Artist Name")
                .withLongOpt("inputArtitsName")
                .create("a"));
        options.addOption(OptionBuilder
                .withArgName("Track Name")
                .hasArg()
                .isRequired(true)
                .withDescription("input Track Name")
                .withLongOpt("inputTrackName")
                .create("t"));
        CommandLine cmd = null;

        try {
            CommandLineParser parser = new GnuParser();
            cmd = parser.parse(options, args);

        } catch (ParseException ex) {

            System.err.println("Command line error : " + ex.getMessage());
            // Logger.getLogger(GenuisFetcher.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        //Input strings :
        String artistname = cmd.getOptionValue("a");
        String trackname = cmd.getOptionValue("t");
        //Output :
        String FinalLyrics = GetLyricsFromArtistNameAndTrackName(artistname, trackname);// Can return null if he doesn't found the requested track
        System.out.println(FinalLyrics);
    }

    /**
     *
     * @param artistname Name of the search artist raw
     * @param trackname Name of the search track raw
     * @return
     */
    public static String GetLyricsFromArtistNameAndTrackName(String artistname, String trackname) {
        String URL = CreateURL(artistname, trackname);// Generate the correct URl to get the html
        Document doc;

        try {
            doc = Jsoup.connect(URL).get();// get the html code
        } catch (IOException ex) {
            System.err.println("Could not get lyrics for " + artistname + " /w track name " + trackname + " /w the URL : " + URL);
            return "";

        }
        String FinalLyrics = PrepareHTML(doc);// remove all the junk from the html to get a great lyrics text
        if (FinalLyrics.isEmpty()) {
            FinalLyrics = "[Instrumental]";// If the track is empty we are assuming that the track is instrumental even if this case normally never occur
        }
        return FinalLyrics;
    }

    private static String FormatName(String TrackName) {
        TrackName = TrackName.replace("(Bonus Track)", "").replaceAll("/", "-").replace("ö", "o").replaceAll("\\s+", "-").replace(",", "").replace("'", "").replace(".", "").replace(" ", "-").toLowerCase(); // fell free to add your own special caracter / words 

        if (String.valueOf(TrackName.charAt(TrackName.length() - 1)).contains("-")) {
            return TrackName.substring(0, TrackName.length() - 1); //remove doubled up -- at the end because of the add of the -lyrics in the URL creator with can occur when their is a space at the end of the track
        }

        return TrackName;
    }

    private static String CreateURL(String artistname, String trackname) {
        String TrackName = FormatName(trackname);
        String ArtistName = FormatName(artistname);
        String url;
        url = "https://genius.com/" + ArtistName + "-" + TrackName + "-lyrics";//regular expression for the url

        return url;
    }

    private static String PrepareHTML(Element doc) {
        String lyrics = doc.select("div.lyrics").toString();
        lyrics = lyrics.replaceAll("<br>", "/n");
        String FinalLyrics = Jsoup.parse(lyrics).text();//Not the most elegant way but whatever it works like a charm
        FinalLyrics = FinalLyrics.replaceAll("/n", "\n");
        return FinalLyrics;
    }
}
