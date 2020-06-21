package org.gregh.PlexTop250Tracker;

public class PlexTop250Tracker {
    public static void main(String[] args) {
        ScrapeIMDBMovieNames IMDBScraper = new ScrapeIMDBMovieNames();
        GrabAllMovieNamesInPlex PlexAPIHitter = new GrabAllMovieNamesInPlex();
        WriteMovieTitlesToExcel excelSheet = new WriteMovieTitlesToExcel();

        // Crosscheck the IMDB list with the Plex library
        PlexAPIHitter.createNewPlexURLWithMovieTitle(IMDBScraper.getMovieTitles());
        // Print out the movies that are missing from the Plex library
        PlexAPIHitter.sendNeededMoviesToFile(PlexAPIHitter.getListOfNeededMovies());
        // Add the movies to an Excel spreadsheet
        excelSheet.writeMissingMoviesToSpreadsheet(PlexAPIHitter.getListOfNeededMovies());
    }
}
