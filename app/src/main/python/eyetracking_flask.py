from flask import Flask, request, jsonify

app = Flask(__name__)
users = [
    {"name": "John Doe", "age": 30}
    ]

@app.route('/')
def home():
    return "Hello, Flask!"

@app.route('/users', methods=['GET'])
def get_users():
    return jsonify(users)

# @app.route('/users', methods=['POST'])
# def add_user():
#     new_user = '{"name": "John Doe", "age": 30}'  # JSON 형식으로 데이터 수신
#     users.append(new_user)
#     return jsonify(new_user), 201 

if __name__ == "__main__":
    app.run(host='192.168.45.221', port=3700)