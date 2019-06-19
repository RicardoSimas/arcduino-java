package serialPort;

public class Protocol {
	
    private String corrente;


    private String leituraComando;

    private void interpretaComando() {
    	
    	String temperatura = leituraComando;
    }

    public String getCorrente() {
		return corrente;
	}

	public void setCorrente(String corrente) {
		this.corrente = corrente;
	}

	public String getLeituraComando() {
        return leituraComando;
    }

    public void setLeituraComando(String leituraComando) {
        this.leituraComando = leituraComando;
        this.interpretaComando();
    }
}
