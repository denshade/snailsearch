import os


def get_hosts():
    dirname = os.path.dirname(os.path.realpath(__file__))
    directory = f"{dirname}/../data"

    hosts = []
    # Iterate over files in directory
    for name in os.listdir(directory):
        # Open file
        if name != "hosts.db":
            hosts.append(name)

    return hosts