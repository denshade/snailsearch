import time
import urllib

from abstr2.context import ContextMap


def do_robot_delay(contextmap):
    rp = contextmap.rp
    if rp is None:
        delay = 1
    else:
        delay = rp.crawl_delay("snail")
    if delay is None:
        delay = 1
    time.sleep(delay)


def load_robots_parser(contextmap: ContextMap) -> ContextMap:
    hostname = contextmap.current_host
    rp = urllib.robotparser.RobotFileParser()
    try:
        with urllib.request.urlopen(urllib.request.Request(f"https://{hostname}/robots.txt",
                                                           headers={'User-Agent': 'Python'})) as response:
            rp.parse(response.read().decode("utf-8").splitlines())
    except:
        print("error load robots.txt")
        rp = None
    contextmap.rp = rp
    return contextmap
