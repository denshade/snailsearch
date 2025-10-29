import sqlite3

def must_contain(words: list):
    filter = ""
    for word in words:
        filter = filter + f" AND text LIKE '%,{word.lower()},%'"
    length = len(" AND ")
    filter = filter[length:]
    return filter


def print_results(results):
    for result in results:
        print(f"{result[0]}")
    print(f"{len(results)} results")


def must_contain_any(words: list):
    filter = ""
    for word in words:
        filter = filter + f" OR text LIKE '%,{word.lower()},%'"
    length = len(" OR ")
    filter = filter[length:]
    return filter


def must_not_contain_all(words: list):
    filter = ""
    for word in words:
        filter = filter + f" AND text NOT LIKE '%,{word.lower()},%'"
    length = len(" AND ")
    filter = filter[length:]
    return filter

def do_filter(cur, or_word_list, and_word_list):
    and_list = must_contain(and_word_list)
    or_list = must_contain_any(or_word_list)
    if and_list == "":
        filter = or_list
    elif or_list == "":
        filter = and_list
    else:
        filter = f"{and_list} AND {or_list}"
    query = f"SELECT URL from site where {filter}"
    res = cur.execute(query)
    results = res.fetchall()
    print_results(results)


con = sqlite3.connect("websites.db")
cur = con.cursor()

do_filter(cur, ["Putin", "Poetin"], [])
