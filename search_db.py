import sqlite3

def must_contain(cur, words: list):
    filter = ""
    for word in words:
        filter = filter + f" AND text LIKE '%,{word.lower()},%'"
    length = len(" AND ")
    filter = filter[length:]
    query = f"SELECT URL from site where {filter}"
    res = cur.execute(query)
    results = res.fetchall()
    print_results(results)


def print_results(results):
    for result in results:
        print(f"{result[0]}")
    print(f"{len(results)} results")


def must_contain_any(cur, words: list):
    filter = ""
    for word in words:
        filter = filter + f" OR text LIKE '%,{word.lower()},%'"
    length = len(" OR ")
    filter = filter[length:]
    query = f"SELECT URL from site where {filter}"
    res = cur.execute(query)
    results = res.fetchall()
    print_results(results)

con = sqlite3.connect("websites.db")
cur = con.cursor()

must_contain_any(cur, ["Vaneeckhaute"])