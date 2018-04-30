import csv, json, sys

if sys.argv[1] is not None and sys.argv[2] is not None:
    for x in range(int(sys.argv[2])):
        controller = sys.argv[1]
        controller += "_" + str(x)

        fileInput = controller + ".txt"
        fileOutput = controller + ".csv"
        inputFile = open(fileInput) # open json file
        outputFile = open(fileOutput, 'w') # load csv file
        data = json.load(inputFile) # load json content
        inputFile.close() # close the input file
        output = csv.writer(outputFile) # create a csv.write

        # Get the total games
        total_games = data.keys()
        header = dict()
        header.update({"game": "0"})
        header.update(data["GameData_0_0"])

        output.writerow(header)  # header row

        # Loop through the games
        for i in range(len(total_games)):
            gameData = []
            game = data["GameData_0_" + str(i)]
            TotalCellsVisisted = data["GameData_0_" + str(i)]["TotalCellsVisisted"]
            controllerName = data["GameData_0_" + str(i)]["ControllerName"]
            levelSize = data["GameData_0_" + str(i)]["levelSize"]
            GameSpaceSearched = data["GameData_0_" + str(i)]["GameSpaceSearched"]
            GameScore = data["GameData_0_" + str(i)]["GameScore"]
            Win = data["GameData_0_" + str(i)]["Win"]
            EndGameTick = data["GameData_0_" + str(i)]["EndGameTick"]
            gameData.append(i)
            gameData.append(TotalCellsVisisted)
            gameData.append(controllerName)
            gameData.append(levelSize)
            gameData.append(GameSpaceSearched)
            gameData.append(GameScore)
            gameData.append(Win)
            gameData.append(EndGameTick)
            output.writerow(gameData)
