import os.path,subprocess
from subprocess import STDOUT,PIPE


def main():
    # Arguments are as follows:
    # 0: int(controller to run) 1: int(number of games) 2: int(game to run)
    simulation_count = 5000
    number_of_controllers = 5
    java_file_location = "../Artefact/gvgai-2016/src/gvgai-2016_jar.jar"
    execute_java(java_file_location, simulation_count, number_of_controllers)


# Execute the java program
def execute_java(java_file, simulationCount, numberOfControllers ):
    gvg_ai_process = subprocess.Popen(["java", "-jar", java_file], stdout=subprocess.PIPE)
    java_returned = gvg_ai_process.stdout.read()
    # print (java_file + java_returned)


if __name__ == '__main__':
        main()