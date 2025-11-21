
def log_processing(visited, urls_to_process, cached, processed):
    if visited % 10 == 1:
        urlsL = len(urls_to_process)
        print(f"count: todo {urlsL} vs visited {visited}, cache {cached} processed {processed}")

