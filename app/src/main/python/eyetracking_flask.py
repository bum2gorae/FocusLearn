from flask import Flask, request, jsonify

app = Flask(__name__)
eye_info = []

@app.route('/')
def home():
    return "Hello, Flask!"

@app.route('/eye_info', methods=['GET'])
def get_eye_info():
    return jsonify(eye_info)

@app.route('/eye_info', methods=['POST'])
def post_eye_info():
    data = request.get_json()
    eye_info.append(data)
    return jsonify({"status": "success", "data": data}), 200

if __name__ == "__main__":
    app.run(host='192.168.0.101', port=3700)