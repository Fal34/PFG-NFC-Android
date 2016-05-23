#!/usr/bin/env python
# -*- coding: utf-8 -*-
 
import telebot 
# Librería de la API del bot.
from telebot import types 
# Tipos para la API del bot.
import time
import datetime
# Librería para hacer que el programa que controla el bot no se acabe.
import random
from random import randint
import re
import sys
reload(sys) 
sys.setdefaultencoding("utf-8")
# Para la codificación en español
import PIL
from PIL import ImageFont
from PIL import Image
from PIL import ImageDraw
import os.path
from os import listdir
from os.path import isfile, join
import urllib, cStringIO
import textwrap
# Para el meme generator
###### Commands #####
'''
meme - Crea tu propio meme dentro de una lista de imágenes
custom_meme - Genera un meme con la imagen que le indiques
simpsons - Responde con la frase de la referencia indicada
help - Ayuda y funciones extras
'''
#####################
 
# TOKEN = '225581471:AAHnOhr8UHLOtgc3GV3yA362EA9wHanu1p4' old bot
TOKEN = '230577173:AAHRoHfAZ60i5CtP7PI-zhhaE3sOpWL-dp0'
api_file_url  ="https://api.telegram.org/file/bot"+TOKEN+"/"

bot = telebot.TeleBot(TOKEN) 
# Creamos el objeto de nuestro bot.
#############################################
#Listener
def listener(messages): 
    # Con esto, estamos definiendo una función llamada 'listener', que recibe como parámetro un dato llamado 'messages'.
    for m in messages: 
        # Por cada dato 'm' en el dato 'messages'
        if m.content_type == 'text': 
            # Filtramos mensajes que sean tipo texto.
            cid = m.chat.id
            date  = datetime.datetime.fromtimestamp( int(m.date) ).strftime('%Y-%m-%d %H:%M:%S')
            source = 'PM' if m.chat.type == 'private' else m.chat.title
            # Almacenaremos el ID de la conversación.
            print "[" + source + "][" + str(date) + "] "+ m.from_user.first_name+": " + m.text 
            # Y haremos que imprima algo parecido a esto -> [52033876] Nick: /start
 
bot.set_update_listener(listener) 
# Así, le decimos al bot que utilice como función escuchadora nuestra función 'listener' declarada arriba.
#############################################
#Variables
insultos = [
    "He visto orcos más agradables de ver que %s",
    "No mereces ni caerte de boca en una mierda %s",
    "%s ojalá te prendan fuego y te apaguen con gasolina",
    "%s tienes menos futuro que Dubovsky haciendo puenting"
    ]
regex_insultos_white_list = [
    "s+e+r+c+r+a+s+h+(\w|\d)*"
    ]

contra_insultos = [
    "Maldito cerdo %s ... buen intento!",
    "Suerte la próxima vez %s"
    ]
#############################################
#Funciones
@bot.message_handler(commands=['start'])   
def initial_send_text_message(m):
    cid = m.chat.id
    text = "Hola! Escribe /help para mostrar la ayuda. Los comandos disponibles aparecen al escribir '/' o en la parte derecha del cuadro de texto"
    bot.send_message(cid,text)
    
@bot.message_handler(commands=['help']) 
def command_help(m): 
    cid = m.chat.id
    bot.send_message(cid,
        '''
        Escribe /<command> help para la ayuda de cada comando.
        \n\nFunciones extras : 
        \nSuma <X> y <Y> - Ejecuta la suma de los valores X e Y
        \nInsulta a <X> - X recibe la ira del bot en forma de insulto
        \nEsta roto Vladimir? - Te devuelve cuan roto está Vladimir (LoL)
        \nHola pharaon - Pharaon en persona te responde con su carcajada
        \nQue tal hace en Tenerife? - Video sobre Forocochia
        '''
        )

@bot.message_handler(commands=['custom_meme']) 
def command_meme_generator_custom(m): 
    # Definimos una función que resuelva lo que necesitemos.
    cid = m.chat.id
    
    # Toma de argumentos
    mens = m.text.split(" ")
    if len(mens) < 2:
        bot.send_message(cid,"Formato incorrecto. Escribe /custom_meme [texto] </bot [texto]> y a continuación se pedirá la imagen de fondo")
        return
    # Check del comando de ayuda
    first_word = mens[1]
    if ( first_word == "help"):
        bot.send_message(cid,"Escribe /custom_meme [texto] </bot [texto]> y a continuación se pedirá la imagen de fondo")
        return
    custom_meme_text = " ".join(mens[1:])
        # Comprobación del tamaño del texto
        
    maxTextLen = 60
    for txt in custom_meme_text.split("/bot"):
        txt.strip()
        if not ( len(txt)<=maxTextLen ):
            bot.send_message(cid,"El texto indicado excede el límite permitido por zona ("+str(maxTextLen)+" caracteres). Prueba a partir el mensaje utilizando '/custom_meme [imagen] [texto_superior] /bot [texto_inferior]'")
            return
    
    custom_meme_author = m.from_user.first_name
    
    ####### Private Method ########
    # Variables para custom_meme
    custom_meme_path = "tmp/temp_custom_meme.png"
    
    # Custom meme generator function
    def custom_meme_generator(m):
        cid = m.chat.id
        
        # Chec de la imagen
        if m.photo is None:
            reply_id = bot.reply_to(m,"No se ha encontrado una imagen. Por favor, responde con una imagen")
            bot.register_for_reply(reply_id , custom_meme_generator)
            return 
        
        # Check para el tamaño de la imagen
        min_photo_size_px = 300
        validImage = False
        for ele in m.photo:
            if ele.width > min_photo_size_px and ele.height > min_photo_size_px:
                validImage = True
                image = ele
                break
            
        if not validImage: 
            reply_id = bot.reply_to(m,"La imagen es demasiado pequeña. Responde con una imagen. Tamaño mínomo : "+ str(min_photo_size_px) + "x" + str(min_photo_size_px) + "px")
            bot.register_for_reply(reply_id , custom_meme_generator)
            return 
        
        file = cStringIO.StringIO(urllib.urlopen(api_file_url + str(bot.get_file(image.file_id).file_path)).read())
        targetPath = "tmp/temp_custom_meme.png"
        # Make photo
        print_text_on_image(file, custom_meme_text, targetPath)
        # Send photo
        bot.send_photo( cid, open( targetPath, 'rb'))
    
    reply_id = bot.reply_to(m,"Ahora respóndeme con la imagen que desees "+ custom_meme_author)
    bot.register_for_reply(reply_id , custom_meme_generator)
    
def print_text_on_image(imageFile , text , final_path):
    # Vars para el manejo de la imagen
    img_fraction = 0.80 # portion of image width you want text width to be
    maxsize = 50
    maxLineLen = 30
    isBot = False
    # Open de la imagen y toma de sus parámetros
    im1 = Image.open(imageFile)
    W, H = im1.size
    # Draw de la imagen preparado para su edición
    draw = ImageDraw.Draw(im1)
    
    position_alignment = 10
    for positional_text in text.split("/bot"):
        # Initial values
        isTextFontSizeSetted = False
        fontsize = 15  # starting font size
        font = ImageFont.truetype("dev/fonts/impact.ttf", fontsize)
        yOffsetCount = 0
        positional_text = positional_text.strip()
        
        if len(positional_text) > 0 :
            splitText = split_every(maxLineLen, positional_text)
            for textElement in splitText:
                
                # Carga del fontsize del texto segun la cantidad del mismo
                if (not isTextFontSizeSetted):
                    maxLengthTextElement = max(splitText, key=len)
                    while font.getsize(maxLengthTextElement)[0] < img_fraction*W and fontsize <= maxsize:
                        # iterate until the text size is just larger than the criteria
                        fontsize += 1
                        font = ImageFont.truetype("dev/fonts/impact.ttf", fontsize)
                    
                    # optionally de-increment to be sure it is less than criteria
                    fontsize -= 1
                    isTextFontSizeSetted = True
                    font = ImageFont.truetype("dev/fonts/impact.ttf", fontsize)
                    textSizeY = font.getsize(maxLengthTextElement)[1]
                    
                w, h = font.getsize(textElement)  
                #w, h = draw.textsize(text)
                x = (W-w)/2
                # y = (H/position_alignment) - (h/2) + yOffsetCount
                if not isBot :
                    # Si es línea única
                    if len(splitText)>1:
                        y = (H/8) - (h/2) + yOffsetCount
                    else:
                        y = (H/10) - (h/2) + yOffsetCount
                else :
                    # Si es línea única
                    if len(splitText)>1:
                        y = (H/1.3) - (h/2) + yOffsetCount
                    else:
                        y = (H/1.2) - (h/2) + yOffsetCount
                
                # Pintado del borde
                border = 2
                borderColor = "black"
                draw.text((x-border, y), textElement, font=font, fill=borderColor)
                draw.text((x-border, y+border), textElement, font=font, fill=borderColor)
                draw.text((x-border, y-border), textElement, font=font, fill=borderColor)
                draw.text((x+border, y), textElement, font=font, fill=borderColor)
                draw.text((x+border, y+border), textElement, font=font, fill=borderColor)
                draw.text((x+border, y-border), textElement, font=font, fill=borderColor)
                draw.text((x, y-border), textElement, font=font, fill=borderColor)
                draw.text((x, y+border), textElement, font=font, fill=borderColor)
                
                # Pintado del texto
                draw.text((x,y), textElement, font=font, fill="white")
                yOffsetCount += textSizeY
            
        # New values for bottom text (inverted)
        isBot = True
    # Save the image with a new name
    im1.save(final_path)
    
    
@bot.message_handler(commands=['meme']) 
def command_meme_generator(m): 
    # Definimos una función que resuelva lo que necesitemos.
    cid = m.chat.id 
    # Opening the file gg.png
    
    # Toma de argumentos
    mens = m.text.split(" ")
    if len(mens) < 2:
        bot.send_message(cid,"Formato incorrecto. Escribe /meme [imagen objetivo]  <[texto]> </bot [texto]>")
        return
    
    # Carga de la imagen objetivo
    target_meme = mens[1]
    # Check for help
    if ( target_meme == "help"):
        memePath = "src/meme/"
        onlyfiles = [f for f in listdir(memePath) if isfile(join(memePath, f))]
        result = "Escribe /meme [imagen] [texto_superior (opcional si se indica /bot)] /bot(opcional) [texto_inferior (opcional)]\nMemes disponibles : "+ str(len(onlyfiles)) + "\n["
        for file in onlyfiles :
            prog = re.compile("meme\_(\w+)\..+", re.IGNORECASE)
            result += prog.match(file).group(1) + ", "
        result = result[:-2] + "]"
        bot.send_message(cid, result)
        return
    
    # Check formato
    if len(mens)<3 :
        bot.send_message(cid,"Formato incorrecto. Escribe /meme [imagen objetivo] <[texto]> </bot [texto]>")
        return
    
    # Comprobación si existe la imagen objetivo
    imageFile = "src/meme/meme_"+target_meme+".jpg"
    text = " ".join(mens[2:])
    if not ( os.path.exists(imageFile) ):
        bot.send_message(cid,"Imagen no existente. Escribe /meme [imagen objetivo] <[texto]> </bot [texto]>")
        return

    # Comprobación del tamaño del texto
    maxTextLen = 60
    for txt in text.split("/bot"):
        if not ( len(txt)<=maxTextLen ):
            bot.send_message(cid,"El texto indicado excede el límite permitido por zona ("+str(maxTextLen)+" caracteres). Prueba a partir el mensaje utilizando '/meme [imagen] [texto_superior] /bot [texto_inferior]'")
            return

    final_path = "tmp/temp_meme.png"
    # Make photo
    print_text_on_image(imageFile, text , final_path)
    # Send photo
    bot.send_photo( cid, open( final_path, 'rb'))
    
# Split text utils
def split_every(n, s):
    return textwrap.wrap(s, width=n)
   
@bot.message_handler(regexp=re.compile("qu[ée] es joselu?.*", re.IGNORECASE | re.UNICODE))
def pregunta_message(m):
    cid = m.chat.id
    bot.send_message(cid,"UN TRAITOOOR!")

@bot.message_handler(regexp=re.compile("est[aá] roto vladimir\?.*", re.IGNORECASE | re.UNICODE))   
def send_video_message(m, video_path = None, text = None):
    cid = m.chat.id
    if video_path is None :
        video_path = 'src/video/ostia_quinn.mp4'
        text = 'Mira si lo está!'
    video = open(video_path, 'rb')
    
    if text is not None:
        bot.send_message(cid,text)
    
    bot.send_video(cid, video)
    
@bot.message_handler(regexp=re.compile("que tal hace en tenerife\?.*", re.IGNORECASE | re.UNICODE))   
def send_video_message_miss_imposible(m):
    send_video_message(m , 'src/video/miss_imposible.mp4', 'Hace sol con SANGRE ...')

@bot.message_handler(regexp=re.compile("hola Pharaon.*", re.IGNORECASE | re.UNICODE))   
def send_audio(m, audio_path = None, text = None):
    cid = m.chat.id
    if audio_path is None :
        audio_path = 'src/audio/pharaon.mp3'
        text = None
    audio = open(audio_path, 'rb')
    
    if text is not None:
        bot.send_message(cid,text)
    bot.send_audio(cid, audio)

simpsons_video_dic = {
    'si era tan listo' : 'https://www.youtube.com/watch?v=_MTITbp4eL8',
    'poli prostituta' : 'https://youtu.be/vF1WuJFE8jk',
    'te invitaria' : 'https://www.youtube.com/watch?v=euE_q676gtM',
    'termodinamica' : 'https://www.youtube.com/watch?v=Rpc2i6tMX2k',
    'eres un lerdo' : 'https://www.youtube.com/watch?v=9XQxBXUE4Jo',
    'hipoglucidos' : 'https://www.youtube.com/watch?v=1KTH6S72MkE',
    'vamos a palmar' : 'https://www.youtube.com/watch?v=n8IyLSv6HdU',
    'nunca' : 'https://www.youtube.com/watch?v=l6POukPBq0E',
    'la tonta esta en el bote' : 'https://www.youtube.com/watch?v=wS8KuiQOZBM',
    'niño rata' : 'https://www.youtube.com/watch?v=QeOLr25vjxc',
    'todos locos' : 'https://www.youtube.com/watch?v=URWLnItt5p0',
    'llevaoslo chumachos' : 'https://www.youtube.com/watch?v=AkSsQ6V2E4U',
    'que bien leer' : 'https://www.youtube.com/watch?v=7aIit_19CaI',
    'un coche azul' : 'https://www.youtube.com/watch?v=ofDRPQ1wYSg',
    'esto es un tio chiflado' : 'https://www.youtube.com/watch?v=ZnpDbQWMWfU',
    'todos somos amigos chico' : 'https://www.youtube.com/watch?v=AoGs5wQtnzs',
    'pa loca tu calva' : 'https://www.youtube.com/watch?v=k3FDVPQnjsE',
    'os habeis cargado el coche?' : 'https://www.youtube.com/watch?v=7Dmn8F26Sts',
    'la caja la caja' : 'https://www.youtube.com/watch?v=i-RJYa04g94',
    'yogurlado' : 'https://www.youtube.com/watch?v=uv3hiVS0u4o',
    'seguro dental' : 'https://www.youtube.com/watch?v=xX3Jrd7zA-I',
    'mi primerito dia' : 'https://www.youtube.com/watch?v=_MP8Aqsh_k0',
    'no digas venganza' : 'https://www.youtube.com/watch?v=0yYGtgFh1FE',
    'es un ladron' : 'https://www.youtube.com/watch?v=6IGXBxhQsW8',
    'taca y a comer' : 'https://www.youtube.com/watch?v=_XT73wdYFPA',
    'mama se llevo las pilas' : 'https://www.youtube.com/watch?v=qnMwsv2QftM',
    'cojo otro muelle' : 'https://www.youtube.com/watch?v=VlsRMA6Q4ec',
    'os prestare toda mi atencion' : 'https://www.youtube.com/watch?v=4XEilgAdKGY',
    'adivina a quien le gustas' : 'https://www.youtube.com/watch?v=TlXzcmdGR-Y',
    'votos a favor' : 'https://www.youtube.com/watch?v=p8SDxWtCADI',
    'eso es mentira so marrana' : 'https://www.youtube.com/watch?v=4YQMpHpLMN0',
    'receto fuego en cantidades' : 'https://www.youtube.com/watch?v=0rX53OHAIpE',
    'no puede comprar un dinosaurio' : 'https://www.youtube.com/watch?v=DLmfO9gG_gA',
    'arranca frena y claxon' : 'https://www.youtube.com/watch?v=0NcJhyy797I',
    'crisistunidad' : 'https://www.youtube.com/watch?v=0BIRf2ILB60',
    'de beber albondigas' : 'https://www.youtube.com/watch?v=jlIDhXw72yk',
    'a la hoguera' : 'https://www.youtube.com/watch?v=vv6QjnEmRK0',
    'corta cava' : 'https://www.youtube.com/watch?v=HOd6jBfatg8',
    'perdiditos' : 'https://www.youtube.com/watch?v=SjhvWja4K-A',
    'con la de hierro que tiene' : 'https://www.youtube.com/watch?v=y3i10UQLLP8',
    'callese jueza' : 'https://www.youtube.com/watch?v=FOqhLjnuyro',
    'que diantres es eso' : 'https://www.youtube.com/watch?v=MrYemvFGxy0',
    'que gran verdad' : 'https://www.youtube.com/watch?v=ASzSb5xFbxc',
    'el hombre del saco' : 'https://www.youtube.com/watch?v=13vl89NEHso',
    'homer lento' : 'https://www.youtube.com/watch?v=0XxpYaytOpA',
    'salida nocturna de homer' : 'https://www.youtube.com/watch?v=79LZtqoYpmg',
    'miau miau' : 'https://www.youtube.com/watch?v=sIoLNuy6gMw',
    'mariquita' : 'https://www.youtube.com/watch?v=VyoBaMXpfFw',
    'sugar' : 'https://www.youtube.com/watch?v=_Vc83Ee8dkM',
    'estaba trompa' : 'https://www.youtube.com/watch?v=p_PymeN9daA',
    'quiero mi bocadillo' : 'https://www.youtube.com/watch?v=RbPAtvZcSsY',
    'bol special k' : 'https://www.youtube.com/watch?v=o3sASk9f6d8',
    'tiren su voto' : 'https://www.youtube.com/watch?v=BtsxqnAKKMk',
    'voy a matar a moe' : 'https://m.youtube.com/watch?v=drW0iGad_VE',
    'sin tele y sin cerveza' : 'https://m.youtube.com/watch?v=sQML1BgulRs'
    }
@bot.message_handler(commands=['simpsons'])   
def send_text_message(m):
    cid = m.chat.id
    
    # Toma de argumentos
    mens = m.text.split(" ")
    if len(mens) < 2 :
        text = "Falta el indicador. Escribe /simpsons [referencia del video] o /simpsons refs para ver las referencias"
        bot.send_message(cid,text)
        return
    
    ref = str(" ".join(mens[1:])).strip()
    
    if ref == "help":
        text = '''\nComando /simpsons. Escribe /simpsons [referencia del video] o /simpsons refs para ver las referencias'''
        bot.send_message(cid,text)
        return
    
    if ref == "refs":
        text = '''\nComando /simpsons. Referencias disponibles:\n['''
        for key in sorted(simpsons_video_dic.keys()):
            text+= str(key) + ", "
        text = str(text[:-2]) + "]"
        bot.send_message(cid,text)
        return 
    
    if ref not in sorted(simpsons_video_dic.keys()):
        text = "Referencia no indicada. Escribe /simpsons [referencia del video] o /simpsons refs para ver las referencias"
        bot.send_message(cid,text)
        return
        
    url = simpsons_video_dic[ref]
    bot.send_message(cid,url)

@bot.message_handler(regexp=re.compile(".*suma (\d+) y (\d+).*", re.IGNORECASE | re.UNICODE))   
def give_video_message(m):
    cid = m.chat.id
    prog = re.compile("suma (\d+) y (\d+)", re.IGNORECASE)
    result = prog.match(m.text)
    x = int(result.group(1));
    y = int(result.group(2));
    bot.send_message(cid,result.group(1)+"+"+result.group(2)+"= "+ str(x+y))
    
@bot.message_handler(regexp=re.compile(".*insulta (a|al) ([\w\d\s]+)", re.IGNORECASE | re.UNICODE))   
def harras_message(m):
    cid = m.chat.id 

    prog = re.compile(".*insulta (a|al) ([\w\d\s]+)", re.IGNORECASE | re.UNICODE)
    result = prog.match(m.text)
    person = str(result.group(2));
    
    print "Persona a insultar : ", person 
    person_from = str(m.from_user.first_name)
    for regex in regex_insultos_white_list:
        prog = re.compile(regex , re.IGNORECASE)
        result = prog.search(person)
        if result :
            print "Se ha encontrado el nombre en la whitelist"
            insult = contra_insultos[randint(0,len(contra_insultos)-1)] % (person_from)
            bot.send_message(cid , insult)
            return
    insult = insultos[randint(0,len(insultos)-1)] % (person)
    bot.send_message(cid , insult)
    
#############################################
#Others
from keepUp import KeepUp
k = KeepUp()
user = "sercrash"
password = "123pimpama"
target_workspace = "https://ide.c9.io/sercrash/joselu-traitor-bot"
k.loadTask(user , password , target_workspace)
#############################################

#Peticiones
bot.polling(none_stop=True) 
# Con esto, le decimos al bot que siga funcionando incluso si encuentra algún fallo.