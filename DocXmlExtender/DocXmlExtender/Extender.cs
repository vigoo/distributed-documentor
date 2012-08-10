using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using System.Xml;

namespace DocXmlExtender
{
    public class Extender
    {
        private readonly Assembly assembly;

        public Extender(Assembly assembly)
        {
            if (assembly == null)
                throw new ArgumentNullException("assembly");

            this.assembly = assembly;
        }

        public void ExtendMemberNode(XmlElement node)
        {
            var doc = node.OwnerDocument;
            if (doc != null)
            {
                var reflectionNode = doc.CreateElement("reflection");
                node.AppendChild(reflectionNode);

                string name = node.GetAttribute("name");

                if (name.Length > 2)
                {
                    char prefix = name[0];
                    string rest = name.Substring(2);

                    switch (prefix)
                    {
                        case 'T':
                            ExtendTypeNode(doc, node, reflectionNode, rest);
                            break;
                        case 'M':
                            ExtendMethodNode(doc, node, reflectionNode, rest);
                            break;
                        case 'P':
                            ExtendPropertyNode(doc, node, reflectionNode, rest);
                            break;
                        case 'F':
                            ExtendFieldNode(doc, node, reflectionNode, rest);
                            break;
                        case 'E':
                            ExtendEventNode(doc, node, reflectionNode, rest);
                            break;
                    }
                }
            }
        }

        private void ExtendTypeNode(XmlDocument doc, XmlElement typeNode, XmlElement reflectionNode, string typeName)
        {
            var type = FindType(typeName);

            if (type != null)
            {
                if (type.IsInterface)
                    reflectionNode.SetAttribute("is-interface", "true");
                else if (type.IsClass)
                    reflectionNode.SetAttribute("is-class", "true");
                else if (type.IsValueType)
                    reflectionNode.SetAttribute("is-value-type", "true");

                AddAttributes(doc, type.GetCustomAttributesData(), reflectionNode);
                AddGenericParameters(doc, reflectionNode, type);
                AddBaseType(doc, reflectionNode, type);
                AddInterfaces(doc, reflectionNode, type);
            }
        }

        private Type FindType(string typeName)
        {
            var type = assembly.GetType(typeName);

            if (type == null)
            {
                // It is possible that this is an inner type, so we first get the owner type
                var lastDotIdx = typeName.LastIndexOf('.');
                var parentTypeName = typeName.Substring(0, lastDotIdx);
                var parentType = assembly.GetType(parentTypeName);
                if (parentType != null)
                {
                    var nestedName = typeName.Substring(lastDotIdx + 1);

                    type = parentType.GetNestedType(nestedName);
                }
            }
            return type;
        }

        private static void AddInterfaces(XmlDocument doc, XmlElement reflectionNode, Type type)
        {
            var ifaces = type.GetInterfaces();
            if (ifaces.Length > 0)
            {
                XmlElement ifacesElem = doc.CreateElement("implements");
                foreach (var iface in ifaces)
                {
                    XmlElement ifaceElem = doc.CreateElement("interface");
                    AddType(doc, iface, ifaceElem);                    
                    ifacesElem.AppendChild(ifaceElem);
                }

                reflectionNode.AppendChild(ifacesElem);
            }
        }

        private static void AddBaseType(XmlDocument doc, XmlElement reflectionNode, Type type)
        {
            if (type.BaseType != null)
            {
                XmlElement extendsElem = doc.CreateElement("extends");
                AddType(doc, type.BaseType, extendsElem);

                reflectionNode.AppendChild(extendsElem);
            }
        }

        private static void AddGenericParameters(XmlDocument doc, XmlElement reflectionNode, Type type)
        {
            if (type.ContainsGenericParameters)
            {
                var genArgs = type.GetGenericArguments();

                XmlElement genArgsElement = doc.CreateElement("genericargs");
                foreach (Type t in genArgs)
                {
                    XmlElement genArgElement = doc.CreateElement("arg");

                    if (t.IsGenericParameter)
                    {
                        genArgElement.SetAttribute("position", XmlConvert.ToString(t.GenericParameterPosition));
                        genArgElement.SetAttribute("name", t.Name);

                        if ((t.GenericParameterAttributes & GenericParameterAttributes.Contravariant) == GenericParameterAttributes.Contravariant)
                            genArgElement.SetAttribute("contravariant", "true");
                        if ((t.GenericParameterAttributes & GenericParameterAttributes.Covariant) == GenericParameterAttributes.Covariant)
                            genArgElement.SetAttribute("covariant", "true");
                        if ((t.GenericParameterAttributes & GenericParameterAttributes.ReferenceTypeConstraint) == GenericParameterAttributes.ReferenceTypeConstraint)
                            genArgElement.SetAttribute("must-be-reference-type", "true");
                        if ((t.GenericParameterAttributes & GenericParameterAttributes.NotNullableValueTypeConstraint) == GenericParameterAttributes.NotNullableValueTypeConstraint)
                            genArgElement.SetAttribute("must-be-not-nullable-value-type", "true");
                        if ((t.GenericParameterAttributes & GenericParameterAttributes.DefaultConstructorConstraint) == GenericParameterAttributes.DefaultConstructorConstraint)
                            genArgElement.SetAttribute("must-have-default-constructor", "true");


                        XmlElement constraintsElement = doc.CreateElement("constraints");
                        foreach (Type constraint in t.GetGenericParameterConstraints())
                        {
                            if (constraint == type)
                            {
                                XmlElement constraintElement = doc.CreateElement("self");
                                constraintsElement.AppendChild(constraintElement);
                            }
                            else
                            {
                                XmlElement constraintElement = doc.CreateElement("interface");
                                AddType(doc, constraint, constraintElement);

                                constraintsElement.AppendChild(constraintElement);
                            }
                        }
                        if (constraintsElement.HasChildNodes)
                            genArgElement.AppendChild(constraintsElement);
                    }
                    else
                    {
                        AddType(doc, t, genArgElement);
                    }


                    genArgsElement.AppendChild(genArgElement);
                }

                reflectionNode.AppendChild(genArgsElement);
            }
        }

        private static void AddType(XmlDocument doc, Type t, XmlElement parent)
        {
            if (t.IsGenericType)
            {
                Type genericType = t.GetGenericTypeDefinition();
                parent.SetAttribute("generic-type", genericType.FullName);

                AddGenericParameters(doc, parent, t);
            }
            else
            {
                parent.SetAttribute("type", t.FullName);
            }
        }

        private void ExtendMethodNode(XmlDocument doc, XmlElement typeNode, XmlElement reflectionNode, string methodName)
        {
            MethodInfo method = null;

            var paramStartIdx = methodName.IndexOf('(');
            if (paramStartIdx != -1)
            {
                var typeNameWithMethodName = methodName.Substring(0, paramStartIdx);
                var dotIdx = typeNameWithMethodName.LastIndexOf('.');
                var typeName = typeNameWithMethodName.Substring(0, dotIdx);
                var methodMemberName = typeNameWithMethodName.Substring(dotIdx + 1);

                var type = FindType(typeName);
                if (type != null)
                {
                    var paramPart = methodName.Substring(paramStartIdx + 1).TrimEnd(')');
                    var paramTypes = paramPart.Split(',');

                    if (paramTypes.Any(p => p.StartsWith("`")))
                    {
                        foreach (var m in type.GetMethods(BindingFlags.NonPublic|BindingFlags.Public|BindingFlags.Instance|BindingFlags.Static))
                        {
                            bool found = true;

                            if (m.Name == methodMemberName)
                            {
                                var mps = m.GetParameters();
                                for (int i = 0; i < mps.Length; i++)
                                {
                                    var paramType = mps[i];
                                    var paramTypeName = paramTypes[i];

                                    if (paramTypeName[0] == '`')
                                    {
                                        try
                                        {
                                            var desiredGenericParamIdx = Convert.ToInt32(paramTypeName.Substring(1));

                                            if (paramType.ParameterType.DeclaringType != type ||
                                                paramType.ParameterType.GenericParameterPosition !=
                                                desiredGenericParamIdx)
                                            {
                                                found = false;
                                            }
                                        }
                                        catch (FormatException)
                                        {
                                            // TODO: handle advanced cases when the owner type's generic argument is not directly used
                                        }
                                    }
                                    else
                                    {
                                        var desiredType = FindType(paramTypeName);
                                        if (desiredType != paramType.ParameterType)
                                            found = false;
                                    }
                                }
                            }
                            else
                            {
                                found = false;
                            }

                            if (found)
                            {
                                method = m;
                                break;
                            }
                        }
                    }
                    else
                    {
                        var parameters = paramTypes.Select(p => FindType(p.Trim())).ToArray();
                        if (parameters.All(p => p != null))
                        {
                            method = type.GetMethod(methodMemberName, BindingFlags.NonPublic|BindingFlags.Public|BindingFlags.Instance|BindingFlags.Static, null, parameters, null);
                        }
                    }
                }
            }
            else
            {
                // No parameters
                
                var dotIdx = methodName.LastIndexOf('.');
                var typeName = methodName.Substring(0, dotIdx);                
                var methodMemberName = methodName.Substring(dotIdx + 1);

                var type = FindType(typeName);
                if (type != null)
                {
                    method = type.GetMethod(methodMemberName, BindingFlags.NonPublic|BindingFlags.Public|BindingFlags.Instance|BindingFlags.Static, null, Type.EmptyTypes, null);
                }
            }

            if (method != null)
            {
                AddAttributes(doc, method.GetCustomAttributesData(), reflectionNode);
                AddReturnValue(doc, method, reflectionNode);
                AddParameters(doc, method, reflectionNode);
            }
        }

        private void AddParameters(XmlDocument doc, MethodInfo method, XmlElement reflectionNode)
        {
            XmlElement parametersElement = doc.CreateElement("parameters");

            foreach (var param in method.GetParameters())
            {
                AddParameter(doc, param, parametersElement);
            }

            reflectionNode.AppendChild(parametersElement);
        }

        private void AddParameter(XmlDocument doc, ParameterInfo pi, XmlElement parametersElement)
        {
            XmlElement paramElement = doc.CreateElement("parameter");
            paramElement.SetAttribute("name", pi.Name);
            paramElement.SetAttribute("is-in", XmlConvert.ToString(pi.IsIn));
            paramElement.SetAttribute("is-out", XmlConvert.ToString(pi.IsOut));
            paramElement.SetAttribute("is-retval", XmlConvert.ToString(pi.IsRetval));
            paramElement.SetAttribute("is-optional", XmlConvert.ToString(pi.IsOptional));
                
            AddType(doc, pi.ParameterType, paramElement);

            parametersElement.AppendChild(paramElement);
        }

        private void AddReturnValue(XmlDocument doc, MethodInfo method, XmlElement reflectionNode)
        {
            XmlElement returnsElement = doc.CreateElement("returns");
            AddType(doc, method.ReturnType, returnsElement);            

            reflectionNode.AppendChild(returnsElement);
        }

        private void ExtendPropertyNode(XmlDocument doc, XmlElement typeNode, XmlElement reflectionNode, string propertyName)
        {
            var dotIdx = propertyName.LastIndexOf('.');
            var typeName = propertyName.Substring(0, dotIdx);
            var propertyMemberName = propertyName.Substring(dotIdx + 1);

            var type = FindType(typeName);
            if (type != null)
            {
                var pi = type.GetProperty(propertyMemberName, BindingFlags.NonPublic | BindingFlags.Public | BindingFlags.Instance | BindingFlags.Static);

                if (pi != null)
                {
                    AddAttributes(doc, pi.GetCustomAttributesData(), reflectionNode);
                    AddPropertyInfo(doc, pi, reflectionNode);                    
                }
            }
        }

        private void AddPropertyInfo(XmlDocument doc, PropertyInfo pi, XmlElement reflectionNode)
        {
            XmlElement propertyElement = doc.CreateElement("property");
            AddType(doc, pi.PropertyType, propertyElement);

            propertyElement.SetAttribute("can-read", XmlConvert.ToString(pi.CanRead));
            propertyElement.SetAttribute("can-write", XmlConvert.ToString(pi.CanWrite));

            reflectionNode.AppendChild(propertyElement);
        }

        private void ExtendFieldNode(XmlDocument doc, XmlElement typeNode, XmlElement reflectionNode, string fieldName)
        {
            var dotIdx = fieldName.LastIndexOf('.');
            var typeName = fieldName.Substring(0, dotIdx);
            var fieldMemberName = fieldName.Substring(dotIdx + 1);

            var type = FindType(typeName);
            if (type != null)
            {
                var field = type.GetField(fieldMemberName, BindingFlags.NonPublic | BindingFlags.Public | BindingFlags.Instance | BindingFlags.Static);

                if (field != null)
                {
                    AddAttributes(doc, field.GetCustomAttributesData(), reflectionNode);
                    AddFieldInfo(doc, field, reflectionNode);
                }
            }
        }
        
        private void AddFieldInfo(XmlDocument doc, FieldInfo field, XmlElement reflectionNode)
        {
            XmlElement fieldElement = doc.CreateElement("field");
            AddType(doc, field.FieldType, fieldElement);

            fieldElement.SetAttribute("is-readonly", XmlConvert.ToString(field.IsInitOnly));

            reflectionNode.AppendChild(fieldElement);
        }

        private void ExtendEventNode(XmlDocument doc, XmlElement typeNode, XmlElement reflectionNode, string eventName)
        {
            var dotIdx = eventName.LastIndexOf('.');
            var typeName = eventName.Substring(0, dotIdx);
            var eventMemberName = eventName.Substring(dotIdx + 1);

            var type = FindType(typeName);
            if (type != null)
            {
                var evt = type.GetEvent(eventMemberName, BindingFlags.NonPublic | BindingFlags.Public | BindingFlags.Instance | BindingFlags.Static);

                if (evt != null)
                {
                    AddAttributes(doc, evt.GetCustomAttributesData(), reflectionNode);
                    AddEventInfo(doc, evt, reflectionNode);
                }
            }
        }

        private void AddEventInfo(XmlDocument doc, EventInfo evt, XmlElement reflectionNode)
        {
            XmlElement evtElement = doc.CreateElement("event");
            AddType(doc, evt.EventHandlerType, evtElement);

            reflectionNode.AppendChild(evtElement);
        }

        private void AddAttributes(XmlDocument doc, IEnumerable<CustomAttributeData> attributes, XmlElement reflectionNode)
        {
            var attribsElem = doc.CreateElement("attributes");

            foreach (var attrib in attributes)
            {
                var attribType = attrib.Constructor.DeclaringType;

                if (attribType != null)
                {
                    var attribElem = doc.CreateElement("attribute");

                    attribElem.SetAttribute("type", attribType.FullName);

                    int i = 0;
                    foreach (var arg in attrib.ConstructorArguments)
                    {
                        var argElem = doc.CreateElement("arg");
                        argElem.SetAttribute("name", attrib.Constructor.GetParameters()[i].Name);
                        argElem.SetAttribute("type", arg.ArgumentType.FullName);
                        argElem.InnerText = Convert.ToString(arg.Value);

                        attribElem.AppendChild(argElem);
                        i++;
                    }

                    if (attrib.NamedArguments != null)
                    {
                        foreach (var narg in attrib.NamedArguments)
                        {
                            var argElem = doc.CreateElement("namedarg");
                            argElem.SetAttribute("name", narg.MemberInfo.Name);
                            argElem.SetAttribute("type", narg.TypedValue.ArgumentType.FullName);
                            argElem.InnerText = Convert.ToString(narg.TypedValue.Value);

                            attribElem.AppendChild(argElem);
                        }
                    }

                    attribsElem.AppendChild(attribElem);
                }
            }

            if (attribsElem.HasChildNodes)
                reflectionNode.AppendChild(attribsElem);
        }

    }
}
