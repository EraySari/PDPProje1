/**
*
* @author Eray Sarı - eray.sari2@ogr.sakarya.edu.tr
* @since 04.09.2023
* <p>
	Bu Main sınıfı verilen java dosyasi icerisindeki  tek satır yorum (// yorumlar), 
	çok satırlı yorum ve javadoc'lari bulup her birini fonksiyonlariyla birlikte txt
	dosyalarina yazdirir ve konsola fonksiyon isimleriyle birlikte yorum sayilarini döndürür
* </p>
*/

package PDPOdev;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;




public class Main {

    public static void main(String[] args) throws IOException {
    	 

        String sorgulanacakDosya = args[0];
        BufferedReader aktar = new BufferedReader(new FileReader(sorgulanacakDosya));
        String line;
        String gecerliFonk = "";
        String cokSatirli = "";
        String javadocYorum = "";
        
        int teksatirSayisi = 0;
        int coksatirSayisi = 0;
        int javadocSayisi = 0;
        
        String yedekTekliler = "";
        String yedekCoklular = "";
        String yedekJavadoc = "";
        
        
        // Yorum dosyalari aciliyor
        FileWriter teksatirYazici = new FileWriter("teksatir.txt");
        FileWriter coklusatirYazici = new FileWriter("coksatir.txt");
        FileWriter javadocYazici = new FileWriter("javadoc.txt");
        
        boolean coksatirMi = false;
        boolean javadocMu = false;
        
        //Kod satir satir inceleniyor
        while ((line = aktar.readLine()) != null) {
        	
            // ------- Fonksiyon Sorgulama -------
            Pattern functionPattern = Pattern.compile("\\s*(public|private|protected)?\\s*\\w+\\s+(\\w+)\\(.*\\)\\s*\\{");
            Matcher functionMatcher = functionPattern.matcher(line);
            if (functionMatcher.matches()) {
            	gecerliFonk = functionMatcher.group(2);
            }
            
        	
            // Fonksiyon sonu - dosyalara ve konsola yazdirma yazdirma    
            if (line.contains("}")) {
            	
        		System.out.println("Fonksiyon: " + gecerliFonk + "\n");
        		System.out.println("Tek Satir Yorum Sayisi: " + teksatirSayisi + "\n");
        		System.out.println("Cok Satir Yorum Sayisi: " + coksatirSayisi + "\n");
        		System.out.println("Javadoc Yorum Sayisi: " + javadocSayisi + "\n");
        		System.out.println("------------------------------------------" + "\n");
        		
        		if(yedekTekliler != "") {
        			teksatirYazici.write("Fonksiyon: " + gecerliFonk + "\n");
        			teksatirYazici.write(yedekTekliler + "\n");
        			teksatirYazici.write("-------------------------------------"+ "\n");
        			yedekTekliler = "";
        		}
        		
        		if(yedekCoklular != "") {
        			coklusatirYazici.write("Fonksiyon: " + gecerliFonk +"\n");
        			int baslangic = yedekCoklular.indexOf("/*");
        			int bitis = yedekCoklular.indexOf("*/");
        			coklusatirYazici.write(yedekCoklular.substring(baslangic, bitis+2)+"\n");

        			coklusatirYazici.write("-------------------------------------"+ "\n");
        			yedekCoklular = "";
        		}
        		if(yedekJavadoc != "") {
        			javadocYazici.write("Fonksiyon: " + gecerliFonk +"\n");
        			int baslangic = yedekJavadoc.indexOf("/**");
        			int bitis = yedekJavadoc.indexOf("*/");
        			javadocYazici.write(yedekJavadoc.substring(baslangic, bitis+2)+"\n");

        			javadocYazici.write("-------------------------------------"+ "\n");
        			yedekJavadoc = "";
        		}

        		
        		gecerliFonk = "";
                teksatirSayisi = 0;
                coksatirSayisi = 0;
                javadocSayisi = 0;
                
                
            }
        	
        	// ----- Tek Satirli icin regex ------
            Pattern commentPattern = Pattern.compile(".*?\\s*\\/\\/\\s*(.*)");
            Matcher commentMatcher = commentPattern.matcher(line);
            if (commentMatcher.matches()) {
                String tekSatirli = commentMatcher.group(1).trim();
                //fonksiyon disindaki tek satirlilar alinmayacak
                if (!tekSatirli.isEmpty() && !gecerliFonk.isEmpty()) {
                	yedekTekliler += tekSatirli + "\n";
                    teksatirSayisi++;
                }
            }
        	
            // ----- Çok satırlı yorumlar için regex ------
            Pattern multilineCommentStartPattern = Pattern.compile(".*?\\s*/\\*(?!\\*).*");
            Pattern multilineCommentEndPattern = Pattern.compile("(.*\\*/\\s*).*");
            Matcher multilineCommentStartMatcher = multilineCommentStartPattern.matcher(line);
            Matcher multilineCommentEndMatcher = multilineCommentEndPattern.matcher(line);
            boolean oneLineMulti = false;
            if (coksatirMi) {
            	
                if (multilineCommentEndMatcher.matches()) {
                	cokSatirli += line.trim() + "\n";

                	coksatirMi = false;
                    
                    if (cokSatirli != "") {
                        yedekCoklular += cokSatirli + "\n";
                    	
                        coksatirSayisi++;
                        cokSatirli = "";
                        
                    }
                } else {

                	cokSatirli += line.trim() + "\n";
                }
            } // Multiline eger tek satir ve fonksiyon icindeyse bu bloga giriyor
            else if (multilineCommentStartMatcher.matches() && multilineCommentEndMatcher.matches() && !gecerliFonk.isEmpty()) {
            	oneLineMulti = true;
                cokSatirli += line.trim();
                yedekCoklular += cokSatirli + "\n";
                cokSatirli = "";
                coksatirSayisi++;

            }
            // Multiline cok satirli ve fonksiyon icindeyse ilk olarak bu bloga giriyor
            else if (multilineCommentStartMatcher.matches() && !oneLineMulti  && !gecerliFonk.isEmpty()) {

            	coksatirMi = true;
   
                cokSatirli += line.trim() + "\n";
            }
            oneLineMulti = false;
            
            //----- Javadoc'lar için regex ----- 
            Pattern javadocStartPattern = Pattern.compile(".*?\\s*/\\*\\*.*");
            Pattern javadocEndPattern = Pattern.compile("(.*\\*/\\s*).*");
            Matcher javadocStartMatcher = javadocStartPattern.matcher(line);
            Matcher javadocEndMatcher = javadocEndPattern.matcher(line);

            if (javadocMu) {
            	
            	javadocYorum += line.trim() + "\n";
            	
                if (javadocEndMatcher.matches()) {   

                	javadocMu = false;
                	yedekJavadoc += javadocYorum + "\n";
                    javadocSayisi++;
                    javadocYorum = "";
                    
                } 
                //Fonksiyon disindakileride alacagimiz icin fonksiyon.isEmpty() sorgulamiyoruz
            } else if (javadocStartMatcher.matches()) {
            	
            	javadocMu = true;
                javadocYorum += line.trim() + "\n";
            }
            
            

            
         
            
        }
        //Yorum dosyalari kapatiliyor
        teksatirYazici.close();
        coklusatirYazici.close();
        javadocYazici.close();
        aktar.close();

    }
}

