import java.util.Random;

import DissertationFiles.DataCollection;
import core.ArcadeMachine;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 04/10/13
 * Time: 16:29
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Test
{
    public static void main(String[] args)
    {
        //Available controllers:
    	String sampleRandomController = "controllers.singlePlayer.sampleRandom.Agent";
    	String doNothingController = "controllers.singlePlayer.doNothing.Agent";
        String sampleOneStepController = "controllers.singlePlayer.sampleonesteplookahead.Agent";
        String sampleMCTSController = "controllers.singlePlayer.sampleMCTS.Agent";
        String sampleFlatMCTSController = "controllers.singlePlayer.sampleFlatMCTS.Agent";
        String sampleOLMCTSController = "controllers.singlePlayer.sampleOLMCTS.Agent";
        String sampleGAController = "controllers.singlePlayer.sampleGA.Agent";
        String sampleOLETSController = "controllers.singlePlayer.olets.Agent";
        String repeatOLETS = "controllers.singlePlayer.repeatOLETS.Agent";

        //! Other controllers
        String YOLOBOT = "YOLOBOT.Agent";
        String breadthFirstSearch27 = "Agent";
        String bestFirstSearch = "controllers.singlePlayer.bestFirstSearch.Agent";
        String breadthFirstSearch = "controllers.singlePlayer.breadthFirstSearch.Agent";
        String breadthFirstSearch2 = "controllers.singlePlayer.breadthFirstSearch2.Agent";
        //String MaastCTS2 = "controllers.singlePlayer.MaastCTS2.Agent";

        String allMCTSControllers[] = new String[]{sampleRandomController, breadthFirstSearch, breadthFirstSearch2, bestFirstSearch ,sampleMCTSController, sampleFlatMCTSController, sampleOLMCTSController};

        //Available Generators
        String randomLevelGenerator = "levelGenerators.randomLevelGenerator.LevelGenerator";
        String geneticGenerator = "levelGenerators.geneticLevelGenerator.LevelGenerator";
        String constructiveLevelGenerator = "levelGenerators.constructiveLevelGenerator.LevelGenerator";
        
        //Available games:
        String gamesPath = "examples/gridphysics/";
        String games[] = new String[]{};
        String deterministicGames[] = new String[]{};
        String stochasticGames[] = new String[]{};
        String generateLevelPath = "examples/gridphysics/";

        //! Edited Alli - 12/02/2018
        boolean useChosenGames = true;
        boolean useInputArguments = true;

        //All public games
        games = new String[]{"aliens", "angelsdemons", "assemblyline", "avoidgeorge", "bait", //0-4
                "blacksmoke", "boloadventures", "bomber", "boulderchase", "boulderdash",      //5-9
                "brainman", "butterflies", "cakybaky", "camelRace", "catapults",              //10-14
                "chainreaction", "chase", "chipschallenge", "clusters", "colourescape",       //15-19
                "chopper", "cookmepasta", "cops", "crossfire", "defem",                       //20-24
                "defender", "digdug", "dungeon", "eggomania", "enemycitadel",                 //25-29
                "escape", "factorymanager", "firecaster",  "fireman", "firestorms",           //30-34
                "freeway", "frogs", "gymkhana", "hungrybirds", "iceandfire",                  //35-39
                "infection", "intersection", "islands", "jaws", "labyrinth",                  //40-44
                "labyrinthdual", "lasers", "lasers2", "lemmings", "missilecommand",           //45-49
                "modality", "overload", "pacman", "painter", "plants",                        //50-54
                "plaqueattack", "portals", "racebet", "raceBet2", "realportals",              //55-59
                "realsokoban", "rivers", "roguelike", "run", "seaquest",                      //60-64
                "sheriff", "shipwreck", "sokoban", "solarfox" ,"superman",                    //65-69
                "surround", "survivezombies", "tercio", "thecitadel", "thesnowman",           //70-74
                "waitforbreakfast", "watergame", "waves", "whackamole", "witnessprotection",  //75-79
                "zelda", "zenpuzzle" };                                                       //80, 81

        if(useChosenGames)
        {
            games = new String[]{"bait", "aliens", "chase", "chopper", "hungrybirds", "digdug", "missilecommand", "intersection", "plaqueattack",
            "seaquest", "camelRace", "butterflies", "escape", "crossfire", "lemmings", "infection", "modality", "roguelike", "waitforbreakfast",
            "survivezombies" };
            // Specific games
            deterministicGames = new String[]{"bait", "chase", "hungrybirds", "missilecommand", "plaqueattack", "camelRace", "escape", "lemmings", "modality", "waitforbreakfast" };
            stochasticGames = new String[]{"aliens", "chopper", "digdug", "intersection", "seaquest", "butterflies", "crossfire", "infection", "roguelike", "survivezombies" };
        }
        int seed = new Random().nextInt();

        //Game and level to play

        int gameIdx = 0;
        int levelIdx = 0; //level names from 0 to 4 (game_lvlN.txt).
        String game = gamesPath + games[gameIdx] + ".txt";
        String level1 = gamesPath + games[gameIdx] + "_lvl" + levelIdx +".txt";

        String recordLevelFile = generateLevelPath + games[gameIdx] + "_glvl.txt";
        //String recordActionsFile = "gameLogs/actions_" + games[gameIdx] + "_lvl" + levelIdx + "_" + seed + ".txt"; //where to record the actions executed. null if not to save.
        String recordActionsFile = "";
        // 1. This starts a game, in a level, played by a human.
         //ArcadeMachine.playOneGame(game, level1, recordActionsFile, seed);


        // 2. This plays a game in a level by the controller.
        //DataCollection.getInstance().ControllerName = breadthFirstSearch;
        //ArcadeMachine.runOneGame(game, level1, !DataCollection.getInstance().headless, breadthFirstSearch, recordActionsFile, seed, 0);

        // 3. This replays a game from an action file previously recorded
        //String readActionsFile = "actions_pacman_lvl1_-1877682670.txt";
        //ArcadeMachine.replayGame(game, level1, visuals, readActionsFile);





        // 4. This plays a single game, in N levels, M times :  Use games.length for all games
        int M = 1;
        int gameID = 0; // Used for processing game from input argument

        // Process any arguments ( first = controller, second = number of games, second = what game to run
        if(args.length > 0 && useInputArguments)
        {
            // Get the controller from first argument
            if (!args[0].isEmpty()) {
                allMCTSControllers = new String[]{allMCTSControllers[Integer.parseInt(args[0])]};
            }
            // Number of games
            if (!args[1].isEmpty()) {
                M = Integer.parseInt(args[1]);
            }
            // What game to run
            if (!args[2].isEmpty())
            {
                games = new String[]{games[Integer.parseInt(args[2])]};
                gameID = Integer.parseInt(args[2]);
            }
        }

        // Run the games
        // Levels to run
        String[] levels = new String[5];
        // controllers to run
        for(int j = 0; j < allMCTSControllers.length; j++)
        {// games to run
            for (int i = 0; i < games.length; i++)
            {
                game = gamesPath + games[i] + ".txt";
                // Set dataCollection singleton values
                DataCollection.getInstance().ControllerName = allMCTSControllers[j];
                DataCollection.getInstance().gameIteration = i;
//
                //Loop through the different levels
                for(int l = 0; l < levels.length; ++l)
                    levels[l] = gamesPath + games[i] + "_lvl" + l + ".txt";
//
                System.out.println("Running " + allMCTSControllers[j] +  " Controller: " + j + " of " + allMCTSControllers.length + ". and game: " + i + " of " + games.length + ". and " + M + " games per level");
                ArcadeMachine.runGames(game, levels, M, allMCTSControllers[j], null, !DataCollection.getInstance().headless);
                DataCollection.getInstance().SaveDataToFile(DataCollection.getInstance().AllData.toString(), allMCTSControllers[j] + "_" + gameID);
            }
        }

        //// Save all game data
        DataCollection.getInstance().SaveDataToFile(DataCollection.getInstance().AllData.toString(), "AllControllersData");
        
        //5. This starts a game, in a generated level created by a specific level generator

        //if(ArcadeMachine.generateOneLevel(game, randomLevelGenerator, recordLevelFile)){
        //	ArcadeMachine.playOneGeneratedLevel(game, recordActionsFile, recordLevelFile, seed);
        //}
        
        //6. This plays N games, in the first L levels, M times each. Actions to file optional (set saveActions to true).
//        int N = 82, L = 5, M = 1;
//        boolean saveActions = false;
//        String[] levels = new String[L];
//        String[] actionFiles = new String[L*M];
//        for(int i = 0; i < N; ++i)
//        {
//            int actionIdx = 0;
//            game = gamesPath + games[i] + ".txt";
//            for(int j = 0; j < L; ++j){
//                levels[j] = gamesPath + games[i] + "_lvl" + j +".txt";
//                if(saveActions) for(int k = 0; k < M; ++k)
//                    actionFiles[actionIdx++] = "actions_game_" + i + "_level_" + j + "_" + k + ".txt";
//            }
//            ArcadeMachine.runGames(game, levels, M, sampleMCTSController, saveActions? actionFiles:null);
//        }
    }
}
