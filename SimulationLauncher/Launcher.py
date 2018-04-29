import subprocess
import threading
import queue

total_games_to_run = 1
number_of_concurrent_games = 4
gameQueue = queue.Queue(number_of_concurrent_games)


# A class to contain the game data for the thread
class SimulationThread (threading.Thread):
    def __init__(self, threadID, name, game, controller, total_games_to_run):
        threading.Thread.__init__(self)
        self.threadID = threadID
        self.name = name
        self.game = game
        self.game_count = total_games_to_run
        self.controller = controller
        self.java_file = "../Artefact/gvgai-2016/gvgai-2016.jar"
    def run(self):
        print("Starting " + self.name)
        # Run the java process
        gvg_ai_process = subprocess.Popen(["java", "-jar", self.java_file, str(self.controller), str(self.game_count), str(self.game)], stdout=subprocess.PIPE)
        java_returned = gvg_ai_process.stdout.read()
        print(java_returned)
        print("Exiting " + self.name)
        # Remove this item from the queue
        gameQueue.get(self)


def main():
    # Arguments for java program are as follows:
    # 0: int(controller to run) 1: int(number of games) 2: int(game to run)
    totalControllers = 4
    totalGames = 20

    # Loop through the controllers and games, and wait for there to be space in the queue
    for i in range(totalControllers):
        for j in range(totalGames):
            add_to_queue(i, j)


# Adds a game thread to the queue
def add_to_queue(controller, game):
        thread = SimulationThread(controller * game, "Game Thread " + str(controller) + "," + str(game), game, controller, total_games_to_run)
        gameQueue.put(thread)
        thread.start()


# Execute the java program
def execute_java(java_file, simulation_count, numberOfControllers ):
    gvg_ai_process = subprocess.Popen(["java", "-jar", java_file, "0", "1", "1"], stdout=subprocess.PIPE)
    print(simulation_count + " " + numberOfControllers)
    java_returned = gvg_ai_process.stdout.read()
    print (java_file + java_returned)


# Main
if __name__ == '__main__':
        main()