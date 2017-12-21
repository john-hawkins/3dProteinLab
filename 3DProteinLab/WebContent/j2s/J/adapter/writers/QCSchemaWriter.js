Clazz.declarePackage ("J.adapter.writers");
Clazz.load (["JU.SB", "J.io.JSONWriter", "java.util.Hashtable", "JV.Viewer"], "J.adapter.writers.QCSchemaWriter", ["java.lang.Boolean", "java.util.Date", "JU.DF", "$.PT", "J.quantum.SlaterData"], function () {
c$ = Clazz.decorateAsClass (function () {
this.moBases = null;
this.filterMOs = false;
this.vwr = null;
this.basisID = 0;
this.shells = null;
this.dfCoefMaps = null;
if (!Clazz.isClassDefined ("J.adapter.writers.QCSchemaWriter.SparseArray")) {
J.adapter.writers.QCSchemaWriter.$QCSchemaWriter$SparseArray$ ();
}
Clazz.instantialize (this, arguments);
}, J.adapter.writers, "QCSchemaWriter", J.io.JSONWriter);
Clazz.prepareFields (c$, function () {
this.moBases =  new java.util.Hashtable ();
});
Clazz.defineMethod (c$, "getUnitsConversion", 
function (units) {
var convFactor = "\"?\"";
units = units.toLowerCase ();
switch ("cm cm^-1 cm-1 angstroms au atomic units".indexOf (units)) {
case 3:
case 9:
units = "cm-1";
break;
case 14:
units = "angstroms";
convFactor = "1.8897";
break;
case 24:
case 27:
units = "au";
convFactor = "1";
break;
}
return "[\"" + units + "\"," + convFactor + "]";
}, "~S");
Clazz.defineMethod (c$, "set", 
function (viewer, os) {
this.vwr = viewer;
this.setStream (os);
}, "JV.Viewer,java.io.OutputStream");
Clazz.defineMethod (c$, "toString", 
function () {
return (this.oc == null ? "{}" : this.oc.toString ());
});
Clazz.defineMethod (c$, "writeJSON", 
function () {
this.openSchema ();
this.writeMagic ();
this.oc.append (",\n");
this.writeSchemaMetadata ();
this.writeJobs ();
this.closeSchema ();
});
Clazz.defineMethod (c$, "writeSchemaMetadata", 
function () {
this.mapOpen ();
this.mapAddKeyValue ("__jmol_created",  new java.util.Date (), ",\n");
this.mapAddKeyValue ("__jmol_source", this.vwr.getP ("_modelFile"), "");
this.mapClose ();
});
Clazz.defineMethod (c$, "openSchema", 
function () {
this.arrayOpen (false);
});
Clazz.defineMethod (c$, "writeMagic", 
function () {
this.writeString (J.adapter.writers.QCSchemaWriter.version);
});
Clazz.defineMethod (c$, "closeSchema", 
function () {
this.oc.append ("\n");
this.arrayClose (false);
this.closeStream ();
});
Clazz.defineMethod (c$, "writeJobs", 
function () {
this.writeJob (1);
});
Clazz.defineMethod (c$, "writeJob", 
function (iJob) {
this.append (",\n");
this.mapOpen ();
{
this.mapAddKeyValue ("__jmol_block", "Job " + iJob, ",\n");
this.writeJobMetadata ();
this.writeModels ();
this.writeMOBases ();
}this.mapClose ();
}, "~N");
Clazz.defineMethod (c$, "writeJobMetadata", 
function () {
this.mapAddKey ("metadata");
this.mapOpen ();
{
this.mapAddMapAllExcept ("__jmol_info", this.vwr.getModelSetAuxiliaryInfo (), ";group3Counts;properties;group3Lists;models;");
}this.mapClose ();
});
Clazz.defineMethod (c$, "writeModels", 
function () {
var nModels = this.vwr.ms.mc;
this.oc.append (",\n");
this.mapAddKey ("steps");
this.arrayOpen (true);
{
this.oc.append ("\n");
for (var i = 0; i < nModels; ) {
if (i > 0) this.append (",\n");
i = this.writeModel (i);
}
}this.arrayClose (true);
});
Clazz.defineMethod (c$, "writeModel", 
function (modelIndex) {
var nextModel = modelIndex + 1;
this.append ("");
this.mapOpen ();
{
this.mapAddKeyValue ("__jmol_block", "Model " + (modelIndex + 1), ",\n");
this.writeTopology (modelIndex);
if (this.isVibration (modelIndex)) {
this.oc.append (",\n");
nextModel = this.writeVibrations (modelIndex);
}if (this.haveMOData (modelIndex)) {
this.oc.append (",\n");
this.writeMOData (modelIndex);
}this.oc.append (",\n");
this.writeModelMetadata (modelIndex);
}this.mapClose ();
this.oc.append ("\n");
return nextModel;
}, "~N");
Clazz.defineMethod (c$, "writeTopology", 
function (modelIndex) {
this.mapAddKey ("topology");
this.mapOpen ();
{
this.writeAtoms (modelIndex);
this.writeBonds (modelIndex);
}this.mapClose ();
}, "~N");
Clazz.defineMethod (c$, "getProperty", 
function (modelIndex, key) {
var props = (modelIndex >= this.vwr.ms.am.length ? null : this.vwr.ms.am[modelIndex].auxiliaryInfo.get ("modelProperties"));
return (props == null ? null : props.get (key));
}, "~N,~S");
Clazz.defineMethod (c$, "isVibration", 
 function (modelIndex) {
return (this.getProperty (modelIndex, "Frequency") != null);
}, "~N");
Clazz.defineMethod (c$, "writeModelMetadata", 
function (modelIndex) {
this.mapAddKey ("metadata");
this.mapOpen ();
{
this.mapAddMapAllExcept ("__jmol_info", this.vwr.ms.am[modelIndex].auxiliaryInfo, ";.PATH;PATH;fileName;moData;");
}this.mapClose ();
}, "~N");
Clazz.defineMethod (c$, "writeAtoms", 
function (modelIndex) {
var symbols = Clazz.innerTypeInstance (J.adapter.writers.QCSchemaWriter.SparseArray, this, null, "_RLE_");
var numbers = Clazz.innerTypeInstance (J.adapter.writers.QCSchemaWriter.SparseArray, this, null, "_RLE_");
var charges = Clazz.innerTypeInstance (J.adapter.writers.QCSchemaWriter.SparseArray, this, null, "_RLE_");
var names = Clazz.innerTypeInstance (J.adapter.writers.QCSchemaWriter.SparseArray, this, null, "_RLE_");
var types = Clazz.innerTypeInstance (J.adapter.writers.QCSchemaWriter.SparseArray, this, null, "_RLE_");
this.mapAddKey ("atoms");
this.mapOpen ();
{
this.writePrefix_Units ("coords_", "Angstroms");
this.mapAddKey ("coords");
this.arrayOpen (true);
{
this.oc.append ("\n");
var bs = this.vwr.getModelUndeletedAtomsBitSet (modelIndex);
var last = bs.length () - 1;
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
var a = this.vwr.ms.at[i];
this.append ("");
this.oc.append (this.formatNumber (a.x)).append (",\t").append (this.formatNumber (a.y)).append (",\t").append (this.formatNumber (a.z)).append (i < last ? ",\n" : "\n");
symbols.add (JU.PT.esc (a.getElementSymbol ()));
numbers.add ("" + a.getElementNumber ());
charges.add ("" + a.getPartialCharge ());
var name = a.getAtomName ();
names.add (name);
var type = a.getAtomType ();
types.add (type.equals (name) ? null : type);
}
}this.arrayClose (true);
this.oc.append (",\n");
if (charges.isNumericAndNonZero ()) {
this.mapAddKeyValueRaw ("charge", charges, ",\n");
}if (types.hasValues ()) {
this.mapAddKeyValueRaw ("types", types, ",\n");
}this.mapAddKeyValueRaw ("symbol", symbols, ",\n");
this.mapAddKeyValueRaw ("atom_number", numbers, "\n");
}this.mapClose ();
}, "~N");
Clazz.defineMethod (c$, "formatNumber", 
 function (x) {
return (x < 0 ? "" : " ") + JU.DF.formatDecimal (x, -6);
}, "~N");
Clazz.defineMethod (c$, "writePrefix_Units", 
 function (prefix, units) {
this.mapAddKeyValueRaw (prefix + "units", this.getUnitsConversion (units), ",\n");
}, "~S,~S");
Clazz.defineMethod (c$, "writeBonds", 
function (modelIndex) {
}, "~N");
Clazz.defineMethod (c$, "writeVibrations", 
function (modelIndex) {
this.mapAddKey ("vibrations");
this.arrayOpen (true);
{
this.oc.append ("\n");
var sep = null;
var ivib = 0;
while (this.isVibration (++modelIndex)) {
if (sep != null) this.oc.append (sep);
sep = ",\n";
this.append ("");
this.mapOpen ();
{
this.mapAddKeyValue ("__jmol_block", "Vibration " + (++ivib), ",\n");
var value = this.getProperty (modelIndex, "FreqValue");
var freq = this.getProperty (modelIndex, "Frequency");
var intensity = this.getProperty (modelIndex, "IRIntensity");
if (value == null) {
System.out.println ("model " + modelIndex + " has no _M.properties.FreqValue");
continue;
}if (freq == null) {
System.out.println ("model " + modelIndex + " has no _M.properties.Frequency");
continue;
}var tokens = JU.PT.split (freq, " ");
if (tokens.length == 1) {
System.out.println ("model " + modelIndex + " has no frequency units");
continue;
}this.writeMapKeyValueUnits ("frequency", value, tokens[1]);
if (intensity != null) {
tokens = JU.PT.split (intensity, " ");
this.writeMapKeyValueUnits ("ir_intensity", tokens[0], tokens[1]);
}var label = this.getProperty (modelIndex, "FrequencyLabel");
if (label != null) this.mapAddKeyValue ("label", label, ",\n");
this.mapAddKey ("vectors");
this.arrayOpen (true);
{
this.oc.append ("\n");
var bs = this.vwr.getModelUndeletedAtomsBitSet (modelIndex);
var last = bs.length () - 1;
for (var i = bs.nextSetBit (0); i >= 0; i = bs.nextSetBit (i + 1)) {
var a = this.vwr.ms.at[i];
var v = a.getVibrationVector ();
this.append ("");
this.oc.append (this.formatNumber (v.x)).append (",\t").append (this.formatNumber (v.y)).append (",\t").append (this.formatNumber (v.z)).append (i < last ? ",\n" : "\n");
}
}this.arrayClose (true);
}this.append ("");
this.mapClose ();
}
}this.oc.append ("\n");
this.arrayClose (true);
return modelIndex;
}, "~N");
Clazz.defineMethod (c$, "writeMapKeyValueUnits", 
 function (key, value, units) {
this.mapAddKeyValueRaw (key, "{\"value\":" + value + ",\"units\":" + this.getUnitsConversion (units) + "}", ",\n");
}, "~S,~O,~S");
Clazz.defineMethod (c$, "haveMOData", 
 function (modelIndex) {
return (this.getAuxiliaryData (modelIndex, "moData") != null);
}, "~N");
Clazz.defineMethod (c$, "getAuxiliaryData", 
 function (modelIndex, key) {
return this.vwr.ms.am[modelIndex].auxiliaryInfo.get (key);
}, "~N,~S");
Clazz.defineMethod (c$, "writeMOData", 
 function (modelIndex) {
var moData = this.getAuxiliaryData (modelIndex, "moData");
var moDataJSON =  new java.util.Hashtable ();
moDataJSON.put ("orbitals", moData.get ("mos"));
var units = moData.get ("EnergyUnits");
if (units == null) units = "?";
moDataJSON.put ("orbitals_energy_units", this.getUnitsConversion (units));
moDataJSON.put ("normalized", Boolean.$valueOf (moData.get ("isNormalized") === Boolean.TRUE));
var type = moData.get ("calculationType");
moDataJSON.put ("calculation_type", type == null ? "?" : type);
moDataJSON.put ("basis_id", this.getBasisID (moData));
this.filterMOs = true;
this.mapAddKeyValue ("molecular_orbitals", moDataJSON, "\n");
this.filterMOs = false;
this.append ("");
}, "~N");
Clazz.overrideMethod (c$, "getAndCheckValue", 
function (map, key) {
if (this.filterMOs) {
if (key.equals ("dfCoefMaps")) return null;
if (key.equals ("symmetry")) return (map.get (key)).$replace ('_', ' ').trim ();
if (key.equals ("coefficients") && this.dfCoefMaps != null) {
return this.fixCoefficients (map.get (key));
}}return map.get (key);
}, "java.util.Map,~S");
Clazz.defineMethod (c$, "fixCoefficients", 
 function (coeffs) {
var c =  Clazz.newDoubleArray (coeffs.length, 0);
for (var i = 0, n = this.shells.size (); i < n; i++) {
var shell = this.shells.get (i);
var type = shell[1];
var map = this.dfCoefMaps[type];
for (var j = 0, coefPtr = 0; j < map.length; j++, coefPtr++) c[coefPtr + j] = coeffs[coefPtr + map[j]];

}
return c;
}, "~A");
Clazz.defineMethod (c$, "getBasisID", 
 function (moData) {
var hash = "!";
this.dfCoefMaps = moData.get ("dfCoefMaps");
if (this.dfCoefMaps != null) {
var haveMap = false;
for (var i = 0; !haveMap && i < this.dfCoefMaps.length; i++) {
var m = this.dfCoefMaps[i];
for (var j = 0; j < m.length; j++) if (m[j] != 0) {
haveMap = true;
break;
}
}
if (!haveMap) this.dfCoefMaps = null;
}var gaussians = moData.get ("gaussians");
if (gaussians != null) {
hash += gaussians.hashCode ();
}this.shells = moData.get ("shells");
if (this.shells != null) {
hash += this.shells.hashCode ();
}var slaters = moData.get ("slaters");
if (slaters != null) {
hash += slaters.hashCode ();
}var key = this.moBases.get (hash);
if (key == null) {
this.moBases.put (hash, key = "MOBASIS_" + ++this.basisID);
var map =  new java.util.Hashtable ();
if (gaussians != null) {
map.put ("gaussians", gaussians);
}if (this.shells != null) {
map.put ("shells", this.shells);
}if (slaters != null) {
map.put ("slaters", slaters);
}this.moBases.put (key, map);
}return key;
}, "java.util.Map");
Clazz.defineMethod (c$, "writeMOBases", 
function () {
if (this.moBases.isEmpty ()) return;
this.oc.append (",\n");
this.mapAddKey ("mo_bases");
this.mapOpen ();
{
var sep = "";
for (var key, $key = this.moBases.keySet ().iterator (); $key.hasNext () && ((key = $key.next ()) || true);) {
if (key.startsWith ("!")) continue;
this.append (sep);
this.mapAddKeyValue (key, this.moBases.get (key), "\n");
sep = ",";
}
}this.mapClose ();
this.moBases.clear ();
});
Clazz.defineMethod (c$, "writeObject", 
function (o) {
if (Clazz.instanceOf (o, J.quantum.SlaterData)) {
this.oc.append (o.toString ());
} else {
Clazz.superCall (this, J.adapter.writers.QCSchemaWriter, "writeObject", [o]);
}}, "~O");
c$.$QCSchemaWriter$SparseArray$ = function () {
Clazz.pu$h(self.c$);
c$ = Clazz.decorateAsClass (function () {
Clazz.prepareCallback (this, arguments);
this.repeatCount = 0;
this.elementCount = 0;
this.$lastElement = null;
this.sep = "";
this.type = null;
this.isRLE = false;
Clazz.instantialize (this, arguments);
}, J.adapter.writers.QCSchemaWriter, "SparseArray", JU.SB);
Clazz.makeConstructor (c$, 
function (a) {
Clazz.superConstructor (this, J.adapter.writers.QCSchemaWriter.SparseArray, []);
this.type = a;
this.isRLE = (a.equals ("_RLE_"));
}, "~S");
Clazz.defineMethod (c$, "add", 
function (a) {
if (a == null) a = "null";
if (!this.isRLE) {
this.append (this.sep);
this.append (a);
this.sep = ",";
return;
}if (this.repeatCount > 0 && !a.equals (this.$lastElement)) {
this.append (this.sep);
this.appendI (this.repeatCount);
this.sep = ",";
this.append (this.sep);
this.append (this.$lastElement);
this.repeatCount = 0;
}this.$lastElement = a;
this.repeatCount++;
this.elementCount++;
}, "~S");
Clazz.defineMethod (c$, "lastElement", 
function () {
return this.$lastElement;
});
Clazz.defineMethod (c$, "isEmpty", 
function () {
return (this.elementCount == 0);
});
Clazz.defineMethod (c$, "allNaN", 
function () {
return (this.allSame () && JU.PT.parseFloat (this.$lastElement) == NaN);
});
Clazz.defineMethod (c$, "allNull", 
function () {
return (this.allSame () && this.$lastElement.equals ("null"));
});
Clazz.defineMethod (c$, "allEmptyString", 
function () {
return (this.allSame () && this.$lastElement.equals (""));
});
Clazz.defineMethod (c$, "allSame", 
function () {
return (!this.isEmpty () && this.elementCount == this.repeatCount);
});
Clazz.defineMethod (c$, "allZero", 
function () {
return (this.allSame () && JU.PT.parseFloat (this.$lastElement) != NaN);
});
Clazz.defineMethod (c$, "hasValues", 
function () {
return (!this.allSame () || !this.allNull () && !this.allEmptyString ());
});
Clazz.defineMethod (c$, "isNumericAndNonZero", 
function () {
return (this.allSame () && !this.allNaN () && !this.allZero ());
});
Clazz.defineMethod (c$, "toString", 
function () {
var a = Clazz.superCall (this, J.adapter.writers.QCSchemaWriter.SparseArray, "toString", []);
return (a.length == 0 ? "[]" : "[\"" + this.type + "\"," + a + (this.repeatCount > 0 ? this.sep + this.repeatCount + "," + this.$lastElement : "") + "]");
});
c$ = Clazz.p0p ();
};
c$.version = c$.prototype.version = "QCJSON 0-0-0.Jmol_" + JV.Viewer.getJmolVersion ().$replace (' ', '_');
Clazz.defineStatics (c$,
"knownUnits", "cm cm^-1 cm-1 angstroms au atomic units");
});
