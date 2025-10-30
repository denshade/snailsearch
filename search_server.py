# Importing flask module in the project is mandatory
# An object of Flask class is our WSGI application.
from flask import Flask, request, render_template

from search_db import search_on_host

# Flask constructor takes the name of
# current module (__name__) as argument.
app = Flask(__name__)


# The route() function of the Flask class is a decorator,
# which tells the application which URL should call
# the associated function.
@app.route('/search', methods=['POST'])
def search():
    and_list = request.form['andlist'].split(" ")
    or_list = request.form['orlist'].split(" ")
    not_list = request.form['notlist'].split(" ")
    urls = search_on_host(or_list, and_list, not_list)
    return render_template('results.html', urls=urls, orlist=" ".join(or_list), andlist=" ".join(and_list), notlist=" ".join(not_list))


@app.route('/')
def home():
    return render_template('index.html')

# main driver function
if __name__ == '__main__':
    # run() method of Flask class runs the application
    # on the local development server.
    app.run()
