import os


def get_hosts():
    directory = r"data"

    hosts = []
    # Iterate over files in directory
    for name in os.listdir(directory):
        # Open file
        if name != "hosts.db":
            hosts.append(name)

    return hosts