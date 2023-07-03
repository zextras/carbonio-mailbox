// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link NormalizeTokenFilter}.
 *
 * @author ysasaki
 * @author smukhopadhyay
 */
public class NormalizeTokenFilterTest {

 @Test
 void alphabet() {
  assertEquals('a', NormalizeTokenFilter.normalize('\uFF21'));
  assertEquals('b', NormalizeTokenFilter.normalize('\uFF22'));
  assertEquals('c', NormalizeTokenFilter.normalize('\uFF23'));
  assertEquals('d', NormalizeTokenFilter.normalize('\uFF24'));
  assertEquals('e', NormalizeTokenFilter.normalize('\uFF25'));
  assertEquals('f', NormalizeTokenFilter.normalize('\uFF26'));
  assertEquals('g', NormalizeTokenFilter.normalize('\uFF27'));
  assertEquals('h', NormalizeTokenFilter.normalize('\uFF28'));
  assertEquals('i', NormalizeTokenFilter.normalize('\uFF29'));
  assertEquals('j', NormalizeTokenFilter.normalize('\uFF2A'));
  assertEquals('k', NormalizeTokenFilter.normalize('\uFF2B'));
  assertEquals('l', NormalizeTokenFilter.normalize('\uFF2C'));
  assertEquals('m', NormalizeTokenFilter.normalize('\uFF2D'));
  assertEquals('n', NormalizeTokenFilter.normalize('\uFF2E'));
  assertEquals('o', NormalizeTokenFilter.normalize('\uFF2F'));
  assertEquals('p', NormalizeTokenFilter.normalize('\uFF30'));
  assertEquals('q', NormalizeTokenFilter.normalize('\uFF31'));
  assertEquals('r', NormalizeTokenFilter.normalize('\uFF32'));
  assertEquals('s', NormalizeTokenFilter.normalize('\uFF33'));
  assertEquals('t', NormalizeTokenFilter.normalize('\uFF34'));
  assertEquals('u', NormalizeTokenFilter.normalize('\uFF35'));
  assertEquals('v', NormalizeTokenFilter.normalize('\uFF36'));
  assertEquals('w', NormalizeTokenFilter.normalize('\uFF37'));
  assertEquals('x', NormalizeTokenFilter.normalize('\uFF38'));
  assertEquals('y', NormalizeTokenFilter.normalize('\uFF39'));
  assertEquals('z', NormalizeTokenFilter.normalize('\uFF3A'));

  assertEquals('a', NormalizeTokenFilter.normalize('\uFF41'));
  assertEquals('b', NormalizeTokenFilter.normalize('\uFF42'));
  assertEquals('c', NormalizeTokenFilter.normalize('\uFF43'));
  assertEquals('d', NormalizeTokenFilter.normalize('\uFF44'));
  assertEquals('e', NormalizeTokenFilter.normalize('\uFF45'));
  assertEquals('f', NormalizeTokenFilter.normalize('\uFF46'));
  assertEquals('g', NormalizeTokenFilter.normalize('\uFF47'));
  assertEquals('h', NormalizeTokenFilter.normalize('\uFF48'));
  assertEquals('i', NormalizeTokenFilter.normalize('\uFF49'));
  assertEquals('j', NormalizeTokenFilter.normalize('\uFF4A'));
  assertEquals('k', NormalizeTokenFilter.normalize('\uFF4B'));
  assertEquals('l', NormalizeTokenFilter.normalize('\uFF4C'));
  assertEquals('m', NormalizeTokenFilter.normalize('\uFF4D'));
  assertEquals('n', NormalizeTokenFilter.normalize('\uFF4E'));
  assertEquals('o', NormalizeTokenFilter.normalize('\uFF4F'));
  assertEquals('p', NormalizeTokenFilter.normalize('\uFF50'));
  assertEquals('q', NormalizeTokenFilter.normalize('\uFF51'));
  assertEquals('r', NormalizeTokenFilter.normalize('\uFF52'));
  assertEquals('s', NormalizeTokenFilter.normalize('\uFF53'));
  assertEquals('t', NormalizeTokenFilter.normalize('\uFF54'));
  assertEquals('u', NormalizeTokenFilter.normalize('\uFF55'));
  assertEquals('v', NormalizeTokenFilter.normalize('\uFF56'));
  assertEquals('w', NormalizeTokenFilter.normalize('\uFF57'));
  assertEquals('x', NormalizeTokenFilter.normalize('\uFF58'));
  assertEquals('y', NormalizeTokenFilter.normalize('\uFF59'));
  assertEquals('z', NormalizeTokenFilter.normalize('\uFF5A'));
 }

 @Test
 void number() {
  assertEquals('0', NormalizeTokenFilter.normalize('\uFF10'));
  assertEquals('1', NormalizeTokenFilter.normalize('\uFF11'));
  assertEquals('2', NormalizeTokenFilter.normalize('\uFF12'));
  assertEquals('3', NormalizeTokenFilter.normalize('\uFF13'));
  assertEquals('4', NormalizeTokenFilter.normalize('\uFF14'));
  assertEquals('5', NormalizeTokenFilter.normalize('\uFF15'));
  assertEquals('6', NormalizeTokenFilter.normalize('\uFF16'));
  assertEquals('7', NormalizeTokenFilter.normalize('\uFF17'));
  assertEquals('8', NormalizeTokenFilter.normalize('\uFF18'));
  assertEquals('9', NormalizeTokenFilter.normalize('\uFF19'));
 }

 /**
  * @see http://en.wikipedia.org/wiki/Trema_(diacritic)
  */
 @Test
 void trema() {
  assertEquals('a', NormalizeTokenFilter.normalize('\u00c4'));
  assertEquals('a', NormalizeTokenFilter.normalize('\u00e4'));
  assertEquals('a', NormalizeTokenFilter.normalize('\u01de'));
  assertEquals('a', NormalizeTokenFilter.normalize('\u01df'));
  assertEquals('e', NormalizeTokenFilter.normalize('\u00cb'));
  assertEquals('e', NormalizeTokenFilter.normalize('\u00eb'));
  assertEquals('h', NormalizeTokenFilter.normalize('\u1e26'));
  assertEquals('h', NormalizeTokenFilter.normalize('\u1e27'));
  assertEquals('i', NormalizeTokenFilter.normalize('\u00cf'));
  assertEquals('i', NormalizeTokenFilter.normalize('\u00ef'));
  assertEquals('i', NormalizeTokenFilter.normalize('\u1e2e'));
  assertEquals('i', NormalizeTokenFilter.normalize('\u1e2f'));
  assertEquals('o', NormalizeTokenFilter.normalize('\u00d6'));
  assertEquals('o', NormalizeTokenFilter.normalize('\u00f6'));
  assertEquals('o', NormalizeTokenFilter.normalize('\u022a'));
  assertEquals('o', NormalizeTokenFilter.normalize('\u022b'));
  assertEquals('o', NormalizeTokenFilter.normalize('\u1e4e'));
  assertEquals('o', NormalizeTokenFilter.normalize('\u1e4f'));
  assertEquals('u', NormalizeTokenFilter.normalize('\u00dc'));
  assertEquals('u', NormalizeTokenFilter.normalize('\u00fc'));
  assertEquals('u', NormalizeTokenFilter.normalize('\u01d5'));
  assertEquals('u', NormalizeTokenFilter.normalize('\u01d6'));
  assertEquals('u', NormalizeTokenFilter.normalize('\u01d7'));
  assertEquals('u', NormalizeTokenFilter.normalize('\u01d8'));
  assertEquals('u', NormalizeTokenFilter.normalize('\u01d9'));
  assertEquals('u', NormalizeTokenFilter.normalize('\u01da'));
  assertEquals('u', NormalizeTokenFilter.normalize('\u01db'));
  assertEquals('u', NormalizeTokenFilter.normalize('\u01dc'));
  assertEquals('u', NormalizeTokenFilter.normalize('\u1e72'));
  assertEquals('u', NormalizeTokenFilter.normalize('\u1e73'));
  assertEquals('u', NormalizeTokenFilter.normalize('\u1e7a'));
  assertEquals('u', NormalizeTokenFilter.normalize('\u1e7b'));
  assertEquals('w', NormalizeTokenFilter.normalize('\u1e84'));
  assertEquals('w', NormalizeTokenFilter.normalize('\u1e85'));
  assertEquals('x', NormalizeTokenFilter.normalize('\u1e8c'));
  assertEquals('x', NormalizeTokenFilter.normalize('\u1e8d'));
  assertEquals('y', NormalizeTokenFilter.normalize('\u0178'));
  assertEquals('y', NormalizeTokenFilter.normalize('\u00ff'));
 }

 /**
  * @see http://en.wikipedia.org/wiki/Katakana
  */
 @Test
 void katakana() {
  assertEquals('\u3041', NormalizeTokenFilter.normalize('\u30A1'));
  assertEquals('\u3042', NormalizeTokenFilter.normalize('\u30A2'));
  assertEquals('\u3043', NormalizeTokenFilter.normalize('\u30A3'));
  assertEquals('\u3044', NormalizeTokenFilter.normalize('\u30A4'));
  assertEquals('\u3045', NormalizeTokenFilter.normalize('\u30A5'));
  assertEquals('\u3046', NormalizeTokenFilter.normalize('\u30A6'));
  assertEquals('\u3047', NormalizeTokenFilter.normalize('\u30A7'));
  assertEquals('\u3048', NormalizeTokenFilter.normalize('\u30A8'));
  assertEquals('\u3049', NormalizeTokenFilter.normalize('\u30A9'));
  assertEquals('\u304A', NormalizeTokenFilter.normalize('\u30AA'));
  assertEquals('\u304B', NormalizeTokenFilter.normalize('\u30AB'));
  assertEquals('\u304C', NormalizeTokenFilter.normalize('\u30AC'));
  assertEquals('\u304D', NormalizeTokenFilter.normalize('\u30AD'));
  assertEquals('\u304E', NormalizeTokenFilter.normalize('\u30AE'));
  assertEquals('\u304F', NormalizeTokenFilter.normalize('\u30AF'));
  assertEquals('\u3051', NormalizeTokenFilter.normalize('\u30B1'));
  assertEquals('\u3052', NormalizeTokenFilter.normalize('\u30B2'));
  assertEquals('\u3053', NormalizeTokenFilter.normalize('\u30B3'));
  assertEquals('\u3054', NormalizeTokenFilter.normalize('\u30B4'));
  assertEquals('\u3055', NormalizeTokenFilter.normalize('\u30B5'));
  assertEquals('\u3056', NormalizeTokenFilter.normalize('\u30B6'));
  assertEquals('\u3057', NormalizeTokenFilter.normalize('\u30B7'));
  assertEquals('\u3058', NormalizeTokenFilter.normalize('\u30B8'));
  assertEquals('\u3059', NormalizeTokenFilter.normalize('\u30B9'));
  assertEquals('\u305A', NormalizeTokenFilter.normalize('\u30BA'));
  assertEquals('\u305B', NormalizeTokenFilter.normalize('\u30BB'));
  assertEquals('\u305C', NormalizeTokenFilter.normalize('\u30BC'));
  assertEquals('\u305D', NormalizeTokenFilter.normalize('\u30BD'));
  assertEquals('\u305E', NormalizeTokenFilter.normalize('\u30BE'));
  assertEquals('\u305F', NormalizeTokenFilter.normalize('\u30BF'));
  assertEquals('\u3061', NormalizeTokenFilter.normalize('\u30C1'));
  assertEquals('\u3062', NormalizeTokenFilter.normalize('\u30C2'));
  assertEquals('\u3063', NormalizeTokenFilter.normalize('\u30C3'));
  assertEquals('\u3064', NormalizeTokenFilter.normalize('\u30C4'));
  assertEquals('\u3065', NormalizeTokenFilter.normalize('\u30C5'));
  assertEquals('\u3066', NormalizeTokenFilter.normalize('\u30C6'));
  assertEquals('\u3067', NormalizeTokenFilter.normalize('\u30C7'));
  assertEquals('\u3068', NormalizeTokenFilter.normalize('\u30C8'));
  assertEquals('\u3069', NormalizeTokenFilter.normalize('\u30C9'));
  assertEquals('\u306A', NormalizeTokenFilter.normalize('\u30CA'));
  assertEquals('\u306B', NormalizeTokenFilter.normalize('\u30CB'));
  assertEquals('\u306C', NormalizeTokenFilter.normalize('\u30CC'));
  assertEquals('\u306D', NormalizeTokenFilter.normalize('\u30CD'));
  assertEquals('\u306E', NormalizeTokenFilter.normalize('\u30CE'));
  assertEquals('\u306F', NormalizeTokenFilter.normalize('\u30CF'));
  assertEquals('\u3071', NormalizeTokenFilter.normalize('\u30D1'));
  assertEquals('\u3072', NormalizeTokenFilter.normalize('\u30D2'));
  assertEquals('\u3073', NormalizeTokenFilter.normalize('\u30D3'));
  assertEquals('\u3074', NormalizeTokenFilter.normalize('\u30D4'));
  assertEquals('\u3075', NormalizeTokenFilter.normalize('\u30D5'));
  assertEquals('\u3076', NormalizeTokenFilter.normalize('\u30D6'));
  assertEquals('\u3077', NormalizeTokenFilter.normalize('\u30D7'));
  assertEquals('\u3078', NormalizeTokenFilter.normalize('\u30D8'));
  assertEquals('\u3079', NormalizeTokenFilter.normalize('\u30D9'));
  assertEquals('\u307A', NormalizeTokenFilter.normalize('\u30DA'));
  assertEquals('\u307B', NormalizeTokenFilter.normalize('\u30DB'));
  assertEquals('\u307C', NormalizeTokenFilter.normalize('\u30DC'));
  assertEquals('\u307D', NormalizeTokenFilter.normalize('\u30DD'));
  assertEquals('\u307E', NormalizeTokenFilter.normalize('\u30DE'));
  assertEquals('\u307F', NormalizeTokenFilter.normalize('\u30DF'));
  assertEquals('\u3081', NormalizeTokenFilter.normalize('\u30E1'));
  assertEquals('\u3082', NormalizeTokenFilter.normalize('\u30E2'));
  assertEquals('\u3083', NormalizeTokenFilter.normalize('\u30E3'));
  assertEquals('\u3084', NormalizeTokenFilter.normalize('\u30E4'));
  assertEquals('\u3085', NormalizeTokenFilter.normalize('\u30E5'));
  assertEquals('\u3086', NormalizeTokenFilter.normalize('\u30E6'));
  assertEquals('\u3087', NormalizeTokenFilter.normalize('\u30E7'));
  assertEquals('\u3088', NormalizeTokenFilter.normalize('\u30E8'));
  assertEquals('\u3089', NormalizeTokenFilter.normalize('\u30E9'));
  assertEquals('\u308A', NormalizeTokenFilter.normalize('\u30EA'));
  assertEquals('\u308B', NormalizeTokenFilter.normalize('\u30EB'));
  assertEquals('\u308C', NormalizeTokenFilter.normalize('\u30EC'));
  assertEquals('\u308D', NormalizeTokenFilter.normalize('\u30ED'));
  assertEquals('\u308E', NormalizeTokenFilter.normalize('\u30EE'));
  assertEquals('\u308F', NormalizeTokenFilter.normalize('\u30EF'));
  assertEquals('\u3091', NormalizeTokenFilter.normalize('\u30F1'));
  assertEquals('\u3092', NormalizeTokenFilter.normalize('\u30F2'));
  assertEquals('\u3093', NormalizeTokenFilter.normalize('\u30F3'));
  assertEquals('\u3094', NormalizeTokenFilter.normalize('\u30F4'));
  assertEquals('\u3095', NormalizeTokenFilter.normalize('\u30F5'));
  assertEquals('\u3096', NormalizeTokenFilter.normalize('\u30F6'));
 }

 /**
  * @see http://en.wikipedia.org/wiki/Katakana
  */
 @Test
 void HalfWidthkatakana() {
  assertEquals('\u3042', NormalizeTokenFilter.normalize('\uFF71'));
  assertEquals('\u3044', NormalizeTokenFilter.normalize('\uFF72'));
  assertEquals('\u3046', NormalizeTokenFilter.normalize('\uFF73'));
  assertEquals('\u3048', NormalizeTokenFilter.normalize('\uFF74'));
  assertEquals('\u304A', NormalizeTokenFilter.normalize('\uFF75'));
  assertEquals('\u304B', NormalizeTokenFilter.normalize('\uFF76'));
  assertEquals('\u304D', NormalizeTokenFilter.normalize('\uFF77'));
  assertEquals('\u304F', NormalizeTokenFilter.normalize('\uFF78'));
  assertEquals('\u3051', NormalizeTokenFilter.normalize('\uFF79'));
  assertEquals('\u3053', NormalizeTokenFilter.normalize('\uFF7A'));
  assertEquals('\u3055', NormalizeTokenFilter.normalize('\uFF7B'));
  assertEquals('\u3057', NormalizeTokenFilter.normalize('\uFF7C'));
  assertEquals('\u3059', NormalizeTokenFilter.normalize('\uFF7D'));
  assertEquals('\u305B', NormalizeTokenFilter.normalize('\uFF7E'));
  assertEquals('\u305D', NormalizeTokenFilter.normalize('\uFF7F'));
  assertEquals('\u305F', NormalizeTokenFilter.normalize('\uFF80'));
  assertEquals('\u3061', NormalizeTokenFilter.normalize('\uFF81'));
  assertEquals('\u3064', NormalizeTokenFilter.normalize('\uFF82'));
  assertEquals('\u3066', NormalizeTokenFilter.normalize('\uFF83'));
  assertEquals('\u3068', NormalizeTokenFilter.normalize('\uFF84'));
  assertEquals('\u306A', NormalizeTokenFilter.normalize('\uFF85'));
  assertEquals('\u306B', NormalizeTokenFilter.normalize('\uFF86'));
  assertEquals('\u306C', NormalizeTokenFilter.normalize('\uFF87'));
  assertEquals('\u306D', NormalizeTokenFilter.normalize('\uFF88'));
  assertEquals('\u306E', NormalizeTokenFilter.normalize('\uFF89'));
  assertEquals('\u306F', NormalizeTokenFilter.normalize('\uFF8A'));
  assertEquals('\u3072', NormalizeTokenFilter.normalize('\uFF8B'));
  assertEquals('\u3075', NormalizeTokenFilter.normalize('\uFF8C'));
  assertEquals('\u3078', NormalizeTokenFilter.normalize('\uFF8D'));
  assertEquals('\u307B', NormalizeTokenFilter.normalize('\uFF8E'));
  assertEquals('\u307E', NormalizeTokenFilter.normalize('\uFF8F'));
  assertEquals('\u307F', NormalizeTokenFilter.normalize('\uFF90'));
  assertEquals('\u3080', NormalizeTokenFilter.normalize('\uFF91'));
  assertEquals('\u3081', NormalizeTokenFilter.normalize('\uFF92'));
  assertEquals('\u3082', NormalizeTokenFilter.normalize('\uFF93'));
  assertEquals('\u3084', NormalizeTokenFilter.normalize('\uFF94'));
  assertEquals('\u3086', NormalizeTokenFilter.normalize('\uFF95'));
  assertEquals('\u3088', NormalizeTokenFilter.normalize('\uFF96'));
  assertEquals('\u3089', NormalizeTokenFilter.normalize('\uFF97'));
  assertEquals('\u308A', NormalizeTokenFilter.normalize('\uFF98'));
  assertEquals('\u308B', NormalizeTokenFilter.normalize('\uFF99'));
  assertEquals('\u308C', NormalizeTokenFilter.normalize('\uFF9A'));
  assertEquals('\u308D', NormalizeTokenFilter.normalize('\uFF9B'));
  assertEquals('\u308F', NormalizeTokenFilter.normalize('\uFF9C'));
  assertEquals('\u3093', NormalizeTokenFilter.normalize('\uFF9D'));
 }
    
    

}
