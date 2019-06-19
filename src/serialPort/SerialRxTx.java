package serialPort;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.Date;
import java.util.Enumeration;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

public class SerialRxTx implements SerialPortEventListener {

	SerialPort serialPort = null;

	private Protocol protocolo = new Protocol();
	private String appArc;

	private BufferedReader input; // objeto para leitura na serial
	private OutputStream output; // objeto para escrita na serial

	private static final int TIME_OUT = 1000; // tempo de espera de comunicacao
	private static int DATA_RATE = 9600; // velocidade da porta serial

	private String serialPortName = "COM9";

	private static final String API_CORRENTE_URL = "https://3b66dca6.ngrok.io/monitoring";

	public boolean iniciaSerial() {
		boolean status = false;

		try {
			// Obtem portas seriais do sistema
			CommPortIdentifier portId = null;
			Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

			while (portId == null && portEnum.hasMoreElements()) {
				CommPortIdentifier currPortId = (CommPortIdentifier) 
						portEnum.nextElement();

				if (currPortId.getName().equals(serialPortName) || 
						currPortId.getName().startsWith(serialPortName)) {
					serialPort = (SerialPort) currPortId.open(appArc, TIME_OUT);
					portId = currPortId;
					System.out.println("Conectado em: " + currPortId.getName());
					break;
				}
			}

			if (portId == null || serialPort == null) {
				return false;
			}

			serialPort.setSerialPortParams(DATA_RATE,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
			status = true;

			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			
			e.printStackTrace();
			status = false;
		}

		return status;
	}

	// Metodo que envia dados pela serial.
	public void sendData(String data) {
		try {
			output = serialPort.getOutputStream();
			output.write(data.getBytes());
		} catch (Exception e) {

			System.err.println(e.toString());
			// retorna um erro, caso ocorra!
		}
	}

	// Metodo que fecha a porta serial
	public synchronized void close() {

		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
		}
	}

	@Override
	public void serialEvent(SerialPortEvent spe) {
		// Metodo para lidar com dados que chegam pela serial

		try {
			switch (spe.getEventType()) {
			case SerialPortEvent.DATA_AVAILABLE:
				if (input == null) {
					input = new BufferedReader(new InputStreamReader(
							serialPort.getInputStream()));
				}

				if (input.ready()) {
					protocolo.setLeituraComando(input.readLine());
					if(protocolo.getLeituraComando() != null) {
						this.send(Double.valueOf(
								protocolo.getLeituraComando()).doubleValue());
System.out.println(Double.valueOf(protocolo.getLeituraComando()).doubleValue());
					}
				}
				break;
			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void send(double corrente) throws JSONException {
		
		double tensao = 220;
		
		Date data = new Date(System.currentTimeMillis());
		
		HttpClient client = HttpClientBuilder.create().build();

		HttpPost post = new HttpPost(API_CORRENTE_URL);
		post.setHeader("Content-type", "application/json");

		JSONObject message = new JSONObject();
		
		message.put("corrente", corrente);
		message.put("tensao", tensao);
		message.put("data", data);

		post.setEntity(new StringEntity(message.toString(), "UTF-8"));
		HttpResponse response = null;
	
		try {
			response = client.execute(post);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static int getDATA_RATE() {
		return DATA_RATE;
	}

	public static void setDATA_RATE(int DATA_RATE) {
		SerialRxTx.DATA_RATE = DATA_RATE;
	}

	public String getSerialPortName() {
		return serialPortName;
	}

	public void setSerialPortName(String serialPortName) {
		this.serialPortName = serialPortName;
	}

}
