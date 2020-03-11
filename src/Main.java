import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    private static List<Konto> konten = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        List<BuchungsCache> caches = new ArrayList<>();

        Konto ebk = null;
        Konto sbk = null;

        BufferedReader csvReader = new BufferedReader(new FileReader("Konten.csv"));
        String row = null;
        boolean isNewKonto = true;
        Konto newKontoCreate = null;
        while ((row = csvReader.readLine()) != null) {
            String[] data = row.split(",");
            if (data.length <= 1) {
                isNewKonto = true;
                continue;
            }

            if (isNewKonto) {
                newKontoCreate = new Konto(data[2], data.length > 6 ? getKontoForName(data[6]) : null, data[5] == "B" ? KontoTyp.BILANZKONTO : data[5] == "A" ? KontoTyp.AKTIV : KontoTyp.PASSIV);
                konten.add(newKontoCreate);
                if (newKontoCreate.getName().equalsIgnoreCase("ebk")) {
                    ebk = newKontoCreate;
                }
                else if (newKontoCreate.getName().equalsIgnoreCase("sbk")) {
                    sbk = newKontoCreate;
                }
            }
            else {
                if (!data[0].equalsIgnoreCase("")) {
                    caches.add(new BuchungsCache(newKontoCreate.getName(), data[0], new BigDecimal(data[1])));
                }
                if (data.length > 2 && !data[3].equalsIgnoreCase("")) {
                    caches.add(new BuchungsCache(data[3], newKontoCreate.getName(), new BigDecimal(data[4])));
                }
            }


            isNewKonto = false;
        }

        if (ebk == null || sbk == null) {
            throw new IllegalStateException("EBK and SBK Konto are necessary");
        }

        for (BuchungsCache buchung : caches) {
            Konto sollKonto = getKontoForName(buchung.soll);
            Konto habenKonto = getKontoForName(buchung.haben);

            if (sollKonto == null || habenKonto == null) {
                throw new IllegalStateException("Buchung für nicht existierendes Konto");
            }

            new Buchung(sollKonto, habenKonto, buchung.betrag);
        }

        for (Konto konto: konten) {
            System.out.println(konto.print());
        }

        String command = IOTools.readLine();
        while (!command.equalsIgnoreCase("exit")) {
            try {
                String[] commandWords = command.split(" ");
                switch (commandWords[0]) {
                    case "create":

                        Konto parentKonto = ebk;
                        if (commandWords.length == 4) {
                            Konto foundKonto = getKontoForName(commandWords[3]);
                            parentKonto = foundKonto == null ? parentKonto : foundKonto;
                        }

                        KontoTyp typ = KontoTyp.PASSIV;
                        if (commandWords.length == 3 && commandWords[2].equalsIgnoreCase("A")) {
                            typ = KontoTyp.AKTIV;
                        }

                        Konto newKonto = new Konto(commandWords[1], parentKonto, typ);
                        konten.add(newKonto);
                        System.out.println(newKonto.print());
                        break;
                    case "b":
                        Konto sollKonto = getKontoForName(commandWords[1]);
                        Konto habenKonto = getKontoForName(commandWords[2]);
                        if (sollKonto == null) {
                            System.out.println("Konto nicht gefunden: " + commandWords[1]);
                        }
                        if (habenKonto == null) {
                            System.out.println("Konto nicht gefunden: " + commandWords[2]);
                        }
                        if (sollKonto == null || habenKonto == null) {
                            break;
                        }

                        BigDecimal betrag = new BigDecimal(commandWords[3]);
                        Buchung buchung = new Buchung(sollKonto, habenKonto, betrag);
                        System.out.println(buchung.erstelleBuchungssatz());
                        System.out.println(sollKonto.print());
                        System.out.println(habenKonto.print());
                        break;
                    case "saldo":
                        for (Konto konto : ebk.getChildKontos()) {
                            konto.saldieren(sbk);
                        }
                    case "print":
                        for (Konto konto : konten) {
                            System.out.println(konto.print());
                        }
                        break;
                    case "del":
                        Konto foundKonto = getKontoForName(commandWords[1]);
                        if (foundKonto != null) {
                            konten.remove(foundKonto);
                            foundKonto.getParentKonto().getChildKontos().remove(foundKonto);
                            foundKonto.getChildKontos().forEach(konto -> konto.setParentKonto(null));
                            System.out.println("delete of " + commandWords[1] + " successfull");
                        }
                        break;
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

    private static Konto getKontoForName(String name) {
        List<Konto> kontos = konten.stream().filter(konto -> konto.getName().equalsIgnoreCase(name)).collect(Collectors.toList());
        return kontos.isEmpty() ? null : kontos.get(0);
    }

    public static class BuchungsCache {
        String soll;
        String haben;
        BigDecimal betrag;

        public BuchungsCache(String soll, String haben, BigDecimal betrag) {
            this.soll = soll;
            this.haben = haben;
            this.betrag = betrag;
        }
    }

}
