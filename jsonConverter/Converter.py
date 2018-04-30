import csv, json, sys

if sys.argv[1] is not None and sys.argv[2] is not None:
    fileInput = sys.argv[1]
    fileOutput = sys.argv[2]
    inputFile = open(fileInput) # open json file
    outputFile = open(fileOutput, 'w') # load csv file
    data = json.load(inputFile) # load json content
    inputFile.close() # close the input file
    output = csv.writer(outputFile) # create a csv.write

    games = data.keys()
    output.writerow(data["GameData_0_0"])  # header row

    for i in range(len(games)): # Loop through the games
        gameData = []
        game = data["GameData_0_" + str(i)]
        TotalCellsVisisted = data["GameData_0_" + str(i)]["TotalCellsVisisted"]
        controllerName = data["GameData_0_" + str(i)]["ControllerName"]
        levelSize = data["GameData_0_" + str(i)]["levelSize"]
        GameSpaceSearched = data["GameData_0_" + str(i)]["GameSpaceSearched"]
        GameScore = data["GameData_0_" + str(i)]["GameScore"]
        Win = data["GameData_0_" + str(i)]["Win"]
        EndGameTick = data["GameData_0_" + str(i)]["EndGameTick"]
        gameData.append(controllerName)
        gameData.append(TotalCellsVisisted)
        gameData.append(levelSize)
        gameData.append(GameSpaceSearched)
        gameData.append(GameScore)
        gameData.append(Win)
        gameData.append(EndGameTick)
        output.writerow(gameData)
