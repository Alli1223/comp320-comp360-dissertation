import csv, json, sys

if sys.argv[1] is not None and sys.argv[2] is not None:
    fileInput = sys.argv[1]
    fileOutput = sys.argv[2]
    inputFile = open(fileInput) # open json file
    outputFile = open(fileOutput, 'w') # load csv file
    data = json.load(inputFile) # load json content
    inputFile.close() # close the input file
    output = csv.writer(outputFile) # create a csv.write

    totalGames = 4
    for i in range(totalGames):
        if(data["GameData_0_" + str(i)] != None):
            game = data["GameData_0_" + str(i)]
            controllerName = data["GameData_0_" + str(i)]["ControllerName"]

            output.writerow(str(controllerName))


    test = data["GameData_0_0"]
    test2 = data["GameData_0_2"]["ControllerName"]
    output.writerow(test.keys())  # header row
    for row in data:
        output.writerow(str(row)) # values rows())
        data[row]

