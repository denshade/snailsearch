import sqlite3
con = sqlite3.connect("data/www.demorgen.be.db")
cur = con.cursor()

res = cur.execute(f"SELECT * from site limit 10")
print(res.fetchall())