package info.lindblad.pedanticpadlock.asset

class StatusBadge(text: String, colorCombination: ColorCombination) {

  val template = """<?xml version="1.0"?>
                      |<svg version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="210" height="210">
                      |    <desc iVinci="yes" version="4.6" gridStep="20" showGrid="no" snapToGrid="no" codePlatform="0"/>
                      |    <g id="Layer1" name="Layer 1" opacity="1">
                      |        <g id="Shape1">
                      |            <desc shapeID="1" type="0" basicInfo-basicType="2" basicInfo-roundedRectRadius="12" basicInfo-polygonSides="6" basicInfo-starPoints="5" bounding="rect(-68.0002,-64.5002,136,129)" text="" font-familyName="Helvetica" font-pixelSize="20" font-bold="0" font-underline="0" font-alignment="1" strokeStyle="0" markerStart="0" markerEnd="0" shadowEnabled="0" shadowOffsetX="0" shadowOffsetY="2" shadowBlur="4" shadowOpacity="160" blurEnabled="0" blurRadius="4" transform="matrix(1.48529,0,0,1.53488,101,99)" pers-center="0,0" pers-size="0,0" pers-start="0,0" pers-end="0,0" locked="0" mesh="" flag=""/>
                      |            <path id="shapePath1" d="M7.62939e-06,18.419 C7.62939e-06,8.246 7.98,7.62939e-06 17.824,7.62939e-06 L184.176,7.62939e-06 C194.02,7.62939e-06 202,8.246 202,18.419 L202,179.581 C202,189.754 194.02,198 184.176,198 L17.824,198 C7.98,198 7.62939e-06,189.754 7.62939e-06,179.581 L7.62939e-06,18.419 Z" style="stroke:none;fill-rule:evenodd;fill:%1$s;fill-opacity:1;"/>
                      |        </g>
                      |        <g id="Shape2">
                      |            <desc shapeID="2" type="0" basicInfo-basicType="2" basicInfo-roundedRectRadius="12" basicInfo-polygonSides="6" basicInfo-starPoints="5" bounding="rect(-67.9998,-64.5001,136,129)" text="" font-familyName="Helvetica" font-pixelSize="20" font-bold="0" font-underline="0" font-alignment="1" strokeStyle="0" markerStart="0" markerEnd="0" shadowEnabled="0" shadowOffsetX="0" shadowOffsetY="2" shadowBlur="4" shadowOpacity="160" blurEnabled="0" blurRadius="4" transform="matrix(1.38971,0,0,1.44961,100.5,98.5)" pers-center="0,0" pers-size="0,0" pers-start="0,0" pers-end="0,0" locked="0" mesh="" flag=""/>
                      |            <path id="shapePath2" d="M6,22.395 C6,12.788 13.466,5 22.676,5 L178.324,5 C187.534,5 195,12.788 195,22.395 L195,174.605 C195,184.212 187.534,192 178.324,192 L22.676,192 C13.466,192 6,184.212 6,174.605 L6,22.395 Z" style="stroke:none;fill-rule:evenodd;fill:%2$s;fill-opacity:1;"/>
                      |        </g>
                      |        <g id="Shape3">
                      |          <text text-anchor="middle" x="100" y="155" fill="white" font-family="Arial" font-size="%4$s" font-weight="900">%3$s</text>
                      |        </g>
                      |    </g>
                      |</svg>""".stripMargin


  override def toString: String = {
    val fontSize = if (text.length == 1) "160" else "140"
    template.format(colorCombination.borderColor, colorCombination.backgroundColor, text, fontSize)
  }

}

case class ColorCombination(backgroundColor: String, borderColor: String) {
}

object Colors {

  val green  = ColorCombination("#5ACA24", "#4EBC13")
  val amber = ColorCombination("#FEC113", "#E2AB00")
  val red = ColorCombination("#FF3328", "#DC1813")
  val grey = ColorCombination("#EEEEEE", "#444444")

}