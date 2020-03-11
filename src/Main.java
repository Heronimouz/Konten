import java.io.Console;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {
        Konto ebk = new Konto("EBK", null, KontoTyp.BILANZKONTO);
//        Konto bga = new Konto("BGA", ebk, KontoTyp.AKTIV);
//        Konto ek = new Konto("EK", ebk, KontoTyp.PASSIV);
//        Konto forderungen = new Konto("Forderungen", ebk, KontoTyp.AKTIV);

//        Konto guv = new Konto("Gewinn und Verlust", ek, KontoTyp.AKTIV);
//        Konto aufwendungen = new Konto("Aufwendungen", guv, KontoTyp.PASSIV);
//        Konto umsatzErlöse = new Konto("UmsatzErlöse", guv, KontoTyp.AKTIV);

        Konto sbk = new Konto("SBK", null, KontoTyp.BILANZKONTO);

//        Buchung bgaAnEbk = new Buchung(bga, ebk, new BigDecimal(30));
//        Buchung ebkAnEk = new Buchung(ebk, ek, new BigDecimal(20));
//        Buchung fordAnEbk = new Buchung(forderungen, ebk, BigDecimal.ZERO);
//        Buchung fordAnUms = new Buchung(forderungen, umsatzErlöse, new BigDecimal(30));
//        aufwendungen.buchen(bga, BigDecimal.TEN);

        for (Konto konto : ebk.getChildKontos()) {
            konto.saldieren(sbk);
        }
//        bga.saldieren(sbk);
//        ek.saldieren(sbk);
//        forderungen.saldieren(sbk);

        System.out.println(ebk.print());

//        System.out.println(bga.print());
//        System.out.println(ek.print());
//        System.out.println(forderungen.print());
//        System.out.println(aufwendungen.print());
//        System.out.println(umsatzErlöse.print());
//        System.out.println(guv.print());

        System.out.println(sbk.print());


        List<Konto> konten = new ArrayList<>();
        konten.add(ebk);
        konten.add(sbk);

        String command = IOTools.readLine();
        while (!command.equalsIgnoreCase("exit")) {
            try {
                String[] commandWords = command.split(" ");
                switch (commandWords[0]) {
                    case "create":
                        // TODO parentKonto
                        Konto newKonto = new Konto(commandWords[1], ebk, KontoTyp.valueOf(commandWords[3]));
                        konten.add(newKonto);
                        System.out.println(newKonto.print());
                        break;
                    case "buchung":
                        Konto sollKonto = konten.stream().filter(konto -> konto.getName().equalsIgnoreCase(commandWords[1])).collect(Collectors.toList()).get(0);
                        Konto habenKonto = konten.stream().filter(konto -> konto.getName().equalsIgnoreCase(commandWords[2])).collect(Collectors.toList()).get(0);
                        BigDecimal betrag = new BigDecimal(commandWords[3]);
                        Buchung buchung = new Buchung(sollKonto, habenKonto, betrag);
                        System.out.println(buchung.erstelleBuchungssatz());
                        System.out.println(sollKonto.print());
                        System.out.println(habenKonto.print());
                        break;
                    case "print":
                        for (Konto konto : konten) {
                            System.out.println(konto.print());
                        }
                    default:
                        System.out.println("unknown command!");
                        break;
                }
            }
            catch (Exception ex) {
                System.out.println("false parameter!");
                System.out.println(ex.getMessage());
            }
            command = IOTools.readLine();
        }
    }

}
