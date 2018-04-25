import os.path,subprocess
from subprocess import STDOUT,PIPE
import threading

threads = []


class myThread (threading.Thread):
    def __init__(self, threadID, name, game):
        threading.Thread.__init__(self)
        self.threadID = threadID
        self.name = name
        self.game = game
        self.java_file = "../Artefact/gvgai-2016/gvgai-2016_jar.jar"
    def run(self):
        print ("Starting " + self.name)
        gvg_ai_process = subprocess.Popen(["java", "-jar", self.java_file, "0", "1", "1"], stdout=subprocess.PIPE)
        java_returned = gvg_ai_process.stdout.read()
        print (self.java_file + java_returned)
        print ("Exiting " + self.name)





def main():
    # Arguments are as follows:
    # 0: int(controller to run) 1: int(number of games) 2: int(game to run)
    simulation_count = 5000
    number_of_controllers = 5
    number_of_concurrent_games = 5
    java_file_location = "../Artefact/gvgai-2016/gvgai-2016_jar.jar"

    # Create new threads
    thread1 = myThread(1, "Thread-1", 1)
    thread2 = myThread(2, "Thread-2", 2)

    for i in range(number_of_concurrent_games):
        thread = myThread(1, "Thread-1", 1)
        threads.append(thread)
        thread.start()

    for thread in threads:  # iterates over the threads
            thread.join()  # waits until the thread has finished work


        # execute_java(java_file_location, simulation_count, number_of_controllers)

    # Execute the java program
def execute_java(java_file, simulation_count, numberOfControllers ):
    gvg_ai_process = subprocess.Popen(["java", "-jar", java_file, "0", "1", "1"], stdout=subprocess.PIPE)
    print(simulation_count + " " + numberOfControllers)
    java_returned = gvg_ai_process.stdout.read()
    print (java_file + java_returned)


if __name__ == '__main__':
        main()