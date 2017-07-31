import { Component, ViewChild } from '@angular/core';
import { IonicPage, NavController, NavParams, ModalController, ViewController, Navbar } from 'ionic-angular';
import { NewqueryPage } from '../newquery/newquery';
import { Http, Headers } from '@angular/http';
import { ChanelsPage } from '../chanels/chanels';

/**
 * Generated class for the QueriesPage page.
 *
 * See http://ionicframework.com/docs/components/#navigation for more info
 * on Ionic pages and navigation.
 */
@IonicPage()
@Component({
  selector: 'page-queries',
  templateUrl: 'queries.html',
})
export class QueriesPage {
    items: any[];
    queryAnswers: {};
    channel: string;
    isRegistered: boolean;
    @ViewChild(Navbar) navBar: Navbar;

    constructor(public navCtrl: NavController, public navParams: NavParams, public modalCtrl: ModalController, http: Http) {
        this.isRegistered = false;
        this.channel = "";
        this.items = [];
        if (this.navParams.get('channel') != undefined) {
            this.channel = this.navParams.get('channel');
            let myHeaders: Headers = new Headers();
            myHeaders.set('Accept', 'text/plain');
            myHeaders.set('Content-Type', 'application/json');
            myHeaders.set('x-okapi-tenant', 'ttt');
            http.get('http://ec2-52-15-97-36.us-east-2.compute.amazonaws.com:8084/buzz?query=channel==' + this.channel, {
                headers: myHeaders
            }).subscribe((resp) => {
                this.items = JSON.parse(resp["_body"]).buzz;
            });
        }

        if (this.navParams.get('registered') != undefined) {
            this.isRegistered = this.navParams.get('registered');
        }

        this.queryAnswers = {};
        
        //this.items.push({
        //    question: 'What is the best weather for traveling in New Zealand?',
        //    tags: ['traveling'],
        //    answers: [
        //        { A: 'Hot', Count: 0 },
        //        { A: 'Cold', Count: 0 },
        //        { A: 'Doesn\'t matter', Count: 0 }
        //    ],
        //    showDetails: false,
        //    totalVotes: 0            
        //});

        //this.items.push({
        //    question: 'Who will win the NBA playoffs?',
        //    tags: ['sports'],
        //    answers: [
        //        { A: 'Cleveland', Count: 0 },
        //        { A: 'Golden State', Count: 0 },
        //        { A: 'San Antonio', Count: 0 }
        //    ],
        //    showDetails: false,
        //    totalVotes: 0
        //});

        //this.items.push({
        //    question: 'What is the best Italian food?',
        //    tags: ['food'],
        //    answers: [
        //        { A: 'Pasta', Count: 0 },
        //        { A: 'Pizza', Count: 0 },
        //        { A: 'Calzone', Count: 0 }
        //    ],
        //    showDetails: false,
        //    totalVotes: 0
        //});

        if (this.navParams.data != undefined && this.navParams.get('item') != undefined) {
            this.items.splice(0, 0, this.navParams.get('item'));
        }
    }

    toggleDetails(data) {
        if (data.showDetails) {
            data.showDetails = false;
            //data.icon = 'ios-add-circle-outline';
        } else {
            data.showDetails = true;
            //data.icon = 'ios-remove-circle-outline';
        }
    }

    submitVote(query, answer) {
        if (this.queryAnswers[query.question] == undefined) {
            for (var i = 0; i < query.answers.length; i++) {
                if (query.answers[i].answer == answer.answer) {
                    query.answers[i].count++;
                }
            }

            query.totalVotes++;
            this.queryAnswers[query.question] = "true";
        }
    }

    openNewQuery() {
        //this.navCtrl.push(NewqueryPage, {});
        let modal = this.modalCtrl.create(NewqueryModal, {});
        modal.onDidDismiss(data => {
            if (data != null) {
                this.items.splice(0, 0, data);
            }
        });

        modal.present();
    }

    backButtonClick() {
        ChanelsPage.loadedData = false;
    }

  ionViewDidLoad() {
    console.log('ionViewDidLoad QueriesPage');
  }

}

@Component({
    template:`
<ion-header>

  <ion-navbar>
    <ion-title>New Query</ion-title>
    <ion-buttons end>
        <button ion-button icon-only (click)="dismiss()">
            <ion-icon name="close"></ion-icon>    
        </button>
    </ion-buttons>
  </ion-navbar>

</ion-header>


<ion-content padding>
    <ion-list>
        <ion-item>
            <ion-label floating>Question</ion-label>
            <ion-input type="text" [(ngModel)]="query.question"></ion-input>
        </ion-item>
        <ion-item>
            <ion-label floating>Answer 1</ion-label>
            <ion-input type="text" [(ngModel)]="query.answer1"></ion-input>
        </ion-item>
        <ion-item>
            <ion-label floating>Answer 2</ion-label>
            <ion-input type="text" [(ngModel)]="query.answer2"></ion-input>
        </ion-item>
        <ion-item>
            <ion-label floating>Answer 3 (Optional)</ion-label>
            <ion-input type="text" [(ngModel)]="query.answer3"></ion-input>
        </ion-item>
        <ion-item>
            <ion-label>Tags</ion-label>
            <ion-select [(ngModel)]="query.tags" multiple="true">
                <ion-option value="sports">Sports</ion-option>
                <ion-option value="traveling">Traveling</ion-option>
                <ion-option value="food">Food</ion-option>
            </ion-select>
        </ion-item>
    </ion-list>
    <button ion-button block (click)="addQuery()">Add</button>
</ion-content>

`
})
export class NewqueryModal {
    query: {};
    constructor(public viewCtrl: ViewController) {
        this.query = {};
    }

    addQuery() {
        var item = {};
        item["question"] = this.query["question"];
        item["totalVotes"] = 0;
        item["showDetails"] = false;
        item["answers"] = [];
        item["answers"].push({
            A: this.query["answer1"],
            Count: 0
        });

        item["answers"].push({
            A: this.query["answer2"],
            Count: 0
        });

        if (this.query["answer3"] != undefined && this.query["answer3"] != "") {
            item["answers"].push({
                A: this.query["answer3"],
                Count: 0
            });
        }

        item["tags"] = this.query["tags"];
        this.viewCtrl.dismiss(item);
    }

    dismiss() {
        this.viewCtrl.dismiss();
    }
}