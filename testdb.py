import sqlite3
con = sqlite3.connect("websites.db")
cur = con.cursor()

res = cur.execute(f"SELECT * from site where url  = 'https://www.vrt.be/vrtnws/nl'")
print(res.fetchall())