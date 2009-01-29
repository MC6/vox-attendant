/**
 * MetaPhone.java
 * (Formerly know as PhoneticList.java)
 *
 * A class to generate phonetic code and keep lists of objects
 * retrievable by a phonetic code.
 *
 * Reference: Computer Language of Dec. 1990, p 39
 *  "Hanging on the Metaphone" by Lawrence Philips
 *
 * This Java implementation, Copyright 1997, William B. Brogden
 * is hereby released for all uses. I would appreciate hearing about it
 * if you find a good use for the class.
 *
 * The static method metaPhone converts an input String into a code.
 * All input is converted to upper case.
 * Limitations: Input format is expected to be a single ASCII word
 * with only characters in the A - Z range, no punctuation or numbers.
 *
 * @date December, 1997
 * @author wbrogden@bga.com    CompuServe  75415,610
 *
 */

/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is vox-mail.
 *
 * The Initial Developer of the Original Code is Voxeo Corporation.
 * Portions created by Voxeo are Copyright (C) 2000-2007.
 * All rights reserved.
 * 
 * Contributor(s):
 * ICOA Inc. <info@icoa.com> (http://icoa.com)
 */
package org.voxattendant.util;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class MetaPhone {

   static String vowels = "AEIOU" ;
   static String frontv = "EIY"   ;
   static String varson = "CSPTG" ;

   static final int maxCodeLen = 4 ;

   /**
    *
    */
   public static String generate( String txt ) {
      int mtsz = 0  ;
      boolean hard = false ;
      if(( txt == null ) ||
         ( txt.length() == 0 )) return "" ;
      // single character is itself
      if( txt.length() == 1 ) return txt.toUpperCase() ;
      //
      char[] inwd = txt.toUpperCase().toCharArray() ;
      //
      String tmpS ;
      StringBuffer local = new StringBuffer( 40 ); // manipulate
      StringBuffer code = new StringBuffer( 10 ) ; //   output
      // handle initial 2 characters exceptions
      switch( inwd[0] ){
        case 'K': case 'G' : case 'P' : /* looking for KN, etc*/
          if( inwd[1] == 'N')local.append(inwd, 1, inwd.length - 1 );
          else local.append( inwd );
          break;
        case 'A': /* looking for AE */
          if( inwd[1] == 'E' )local.append(inwd, 1, inwd.length - 1 );
          else local.append( inwd );
          break;
        case 'W' : /* looking for WR or WH */
          if( inwd[1] == 'R' ){   // WR -> R
            local.append(inwd, 1, inwd.length - 1 ); break ;
          }
          if( inwd[1] == 'H'){
            local.append(inwd, 1, inwd.length - 1 );
            local.setCharAt( 0,'W'); // WH -> W
          }
          else local.append( inwd );
          break;
        case 'X' : /* initial X becomes S */
          inwd[0] = 'S' ;local.append( inwd );
          break ;
        default :
          local.append( inwd );
      } // now local has working string with initials fixed
      int wdsz = local.length();
      int n = 0 ;
      while((mtsz < maxCodeLen ) && // max code size of 4 works well
            (n < wdsz ) ){
        char symb = local.charAt(n) ;
        // remove duplicate letters except C
        if(( symb != 'C' ) &&
           (n > 0 ) && ( local.charAt(n - 1 ) == symb )) n++ ;
        else{ // not dup
          switch( symb ){
            case 'A' : case 'E' : case 'I' : case 'O' : case 'U' :
              if( n == 0 ) { code.append(symb );mtsz++;
              }
              break ; // only use vowel if leading char
            case 'B' :
              if( (n > 0 ) &&
                  !(n + 1 == wdsz ) && // not MB at end of word
                  ( local.charAt(n - 1) == 'M')) {
                    code.append(symb);
                  }
              else code.append(symb);
              mtsz++ ;
              break ;
            case 'C' : // lots of C special cases
              /* discard if SCI, SCE or SCY */
              if( ( n > 0 ) &&
                  ( local.charAt(n-1) == 'S' ) &&
                  ( n + 1 < wdsz ) &&
                  ( frontv.indexOf( local.charAt(n + 1)) >= 0 )){ break ;}
              tmpS = local.toString();
              if( tmpS.indexOf("CIA", n ) == n ) { // "CIA" -> X
                 code.append('X' ); mtsz++; break ;
              }
              if( ( n + 1 < wdsz ) &&
                  (frontv.indexOf( local.charAt(n+1) )>= 0 )){
                 code.append('S');mtsz++; break ; // CI,CE,CY -> S
              }
              if(( n > 0) &&
                 ( tmpS.indexOf("SCH",n-1 )== n-1 )){ // SCH->sk
                 code.append('K') ; mtsz++;break ;
              }
              if( tmpS.indexOf("CH", n ) == n ){ // detect CH
                if((n == 0 ) &&
                   (wdsz >= 3 ) &&    // CH consonant -> K consonant
                   (vowels.indexOf( local.charAt( 2) ) < 0 )){
                     code.append('K');
                }
                else { code.append('X'); // CHvowel -> X
                }
                mtsz++;
              }
              else { code.append('K' );mtsz++;
              }
              break ;
            case 'D' :
              if(( n + 2 < wdsz )&&  // DGE DGI DGY -> J
                 ( local.charAt(n+1) == 'G' )&&
                 (frontv.indexOf( local.charAt(n+2) )>= 0)){
                    code.append('J' ); n += 2 ;
              }
              else { code.append( 'T' );
              }
              mtsz++;
              break ;
            case 'G' : // GH silent at end or before consonant
              if(( n + 2 == wdsz )&&
                 (local.charAt(n+1) == 'H' )) break ;
              if(( n + 2 < wdsz ) &&
                 (local.charAt(n+1) == 'H' )&&
                 (vowels.indexOf( local.charAt(n+2)) < 0 )) break ;
              tmpS = local.toString();
              if((n > 0) &&
                 ( tmpS.indexOf("GN", n ) == n)||
                 ( tmpS.indexOf("GNED",n) == n )) break ; // silent G
              if(( n > 0 ) &&
                 (local.charAt(n-1) == 'G')) hard = true ;
              else hard = false ;
              if((n+1 < wdsz) &&
                 (frontv.indexOf( local.charAt(n+1) ) >= 0 )&&
                 (!hard) ) code.append( 'J' );
              else code.append('K');
              mtsz++;
              break ;
            case 'H':
              if( n + 1 == wdsz ) break ; // terminal H
              if((n > 0) &&
                 (varson.indexOf( local.charAt(n-1)) >= 0)) break ;
              if( vowels.indexOf( local.charAt(n+1)) >=0 ){
                  code.append('H') ; mtsz++;// Hvowel
              }
              break;
            case 'F': case 'J' : case 'L' :
            case 'M': case 'N' : case 'R' :
              code.append( symb ); mtsz++; break ;
            case 'K' :
              if( n > 0 ){ // not initial
                if( local.charAt( n -1) != 'C' ) {
                     code.append(symb );
                }
              }
              else   code.append( symb ); // initial K
              mtsz++ ;
              break ;
            case 'P' :
              if((n + 1 < wdsz) &&  // PH -> F
                 (local.charAt( n+1) == 'H'))code.append('F');
              else code.append( symb );
              mtsz++;
              break ;
            case 'Q' :
              code.append('K' );mtsz++; break ;
            case 'S' :
              tmpS = local.toString();
              if((tmpS.indexOf("SH", n )== n) ||
                 (tmpS.indexOf("SIO",n )== n) ||
                 (tmpS.indexOf("SIA",n )== n)) code.append('X');
              else code.append( 'S' );
              mtsz++ ;
              break ;
            case 'T' :
              tmpS = local.toString(); // TIA TIO -> X
              if((tmpS.indexOf("TIA",n )== n)||
                 (tmpS.indexOf("TIO",n )== n) ){
                    code.append('X'); mtsz++; break;
              }
              if( tmpS.indexOf("TCH",n )==n) break;
              // substitute numeral 0 for TH (resembles theta after all)
              if( tmpS.indexOf("TH", n )==n) code.append('0');
              else code.append( 'T' );
              mtsz++ ;
              break ;
            case 'V' :
              code.append('F'); mtsz++;break ;
            case 'W' : case 'Y' : // silent if not followed by vowel
              if((n+1 < wdsz) &&
                 (vowels.indexOf( local.charAt(n+1))>=0)){
                    code.append( symb );mtsz++;
              }
              break ;
            case 'X' :
              code.append('K'); code.append('S');mtsz += 2;
              break ;
            case 'Z' :
              code.append('S'); mtsz++; break ;
          } // end switch
          n++ ;
        } // end else from symb != 'C'
        if( mtsz > 4 )code.setLength( 4);
      }
      return code.toString();
    } // end static method metaPhone()

   // instance variables
   private int objectCt = 0 ;
   private Hashtable basicH ;

   // no param constructor assumes a modest list size
   public MetaPhone () {
      this(1000);
   }

   /** Use this constructor if you have a good guess as to the final
    * size needed. The standard Hashtable load factor of 0.75 means
    * that the Hashtable will automatically expand when it has
    * reached  Capacity * load factor  items. For a final 1000 codes
    * this means a capacity of 1334 will not cause expansion. Hashtable
    * expansion proceeds by doubling so a good guess at the start will
    * save memory and execution time, but is not strictly speaking
    * necessary.
    */
   public MetaPhone ( int guess ) {
      basicH = new Hashtable( guess );
   }

   /**
    * Same syntax as Hashtable put except only Strings can be a key.
    * Returns the code under which the object is stored.
    * NOTE: this version does NOT check for duplicate objects.
    */
   public String put( String key, Object value ) {
      String cd = generate( key );
      if( basicH.containsKey( cd ) ){
         Object prev = basicH.get( cd ) ;
         if( prev instanceof Vector ){
            ((Vector)prev).addElement( value );
         }
       else {  // not a vector, must start one
         Vector V = new Vector();  // default capacity is 10
         V.addElement( prev );
         V.addElement( value );
         basicH.put( cd, V );
       }
    }
    else { basicH.put( cd, value ) ;
    }
    objectCt++ ;
    return cd ;
   }
   /**
    *
    */
   public int getCount() {
      return objectCt;
   }
   /**
    * Returns either null if no match or an array of type Object.
    * The array size could be 1 or greater
    */
   public Object[] match(String txt ){
    Object[] ret = null ;
    String key = generate( txt );
    Object val = basicH.get( key );
    if( val == null ) return ret ;
    if( val instanceof String ){
      ret = new Object[1]; ret[0] = val;
    }
    else { Vector V = (Vector) val ;
      int n = V.size();
      ret = new Object[ n ];
      for(int i = 0 ; i < n ; i++ ){
        ret[i] =  V.elementAt(i);
      }
    }
    return ret ;
  }

   // This is included as a handy debugging tool. The PrintStream can
   // be System.out or derived from a FileOutputStream.
   public void dump(PrintStream p){
    p.println("Basic size = " + basicH.size());
    Enumeration E = basicH.keys();
    while( E.hasMoreElements() ){
      Object key = E.nextElement();
      Object val = basicH.get( key );
      p.print( key + ": " );
      if( val instanceof Vector ) {
        Vector V = (Vector) val ;
        int n = V.size() - 1 ;
        for(int i = 0 ; i < n ; i++ ){
           p.print( V.elementAt(i) + " / " );
        }
        p.println( V.lastElement() );
      }
      else { p.println( val ) ;
      }
    }
   }
   /**
    * The Main for test purposes...
    */
   public static void main(String[] args) {
      //System.out.println("Code for is: " + generate("jon gomes"));
      //System.out.println("Code for is: " + generate("john gomes"));
      for(int i=0; i < args.length; i++) {
         System.out.println("Code for " + args[i] + " is " + generate(args[i]));
      }
   }
}